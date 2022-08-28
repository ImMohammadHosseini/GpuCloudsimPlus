package org.cloudbus.cloudsim.gp.resources;

import java.util.*;
import java.util.function.Predicate;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.util.BytesConversion;
import org.gpucloudsimplus.listeners.GpuEventInfo;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;
import org.cloudbus.cloudsim.resources.ResourceManageableAbstract;
import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisioner;
import org.gpucloudsimplus.listeners.GpuUpdatesVgpusProcessingEventInfo;
import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuSchedulerSpaceShared;
import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisionerSimple;



public class GpuSimple implements Gpu {
	
	private static long defaultGddramCapacity = (long) BytesConversion.gigaToMega(10);
    private static long defaultBwCapacity = 1000;
    
	private long id;
	private String type;
	private final Ram ram;
    private final Bandwidth bw;
	private List<Pe> gpuCoreList;
	private GpuResourceProvisioner gpuGddramProvisioner;
	private GpuResourceProvisioner gpuBwProvisioner;
	
    private final List<GpuStateHistoryEntry> stateHistory;

    private boolean activateOnVideocardStartup;
    private boolean failed;
    private boolean active;
    private boolean stateHistoryEnabled;
    private double startTime = -1;
    private double firstStartTime = -1;
    private double shutdownTime;
    private double totalUpTime;
    private double lastBusyTime;
    private double idleShutdownDeadline;
    
    private VGpuScheduler vgpuScheduler;
    private final List<CustomVGpu> vgpuList = new ArrayList<>();

    private final Set<CustomVGpu> vgpusMigratingIn;
    private final Set<CustomVGpu> vgpusMigratingOut;
    
    private Videocard videocard;

    private final Set<EventListener<GpuUpdatesVgpusProcessingEventInfo>> onUpdateProcessingListeners;
    private final List<EventListener<GpuEventInfo>> onStartupListeners;
    private final List<EventListener<GpuEventInfo>> onShutdownListeners;

    private List<ResourceManageable> resources;

    private List<GpuResourceProvisioner> provisioners;
    private final List<CustomVGpu> vgpuCreatedList;
    
    private int freeCoresNumber;
    private int busyCoresNumber;
    private int workingCoresNumber;
    private int failedCoresNumber;
    private boolean lazySuitabilityEvaluation;
	
    
    public GpuSimple (long id,
    		final GpuResourceProvisioner gpuGddramProvisioner,
            final GpuResourceProvisioner gpubwProvisioner,
            final long storage,
            final List<Pe> coreList) {
    	
    	this(id, "", gpuGddramProvisioner.getCapacity(), gpubwProvisioner.getCapacity(), coreList, 
    			true);
    	setGpuGddramProvisioner(gpuGddramProvisioner);
    	setGpuBwProvisioner(gpubwProvisioner);
	}
    
	public GpuSimple (long id, String type, final long ram, final long bw,
			final List<Pe> coreList, final boolean activate) {
		this.setId(id);
		this.setType(type);
		
		this.idleShutdownDeadline = DEF_IDLE_SHUTDOWN_DEADLINE;
        this.lazySuitabilityEvaluation = true;

        this.ram = new Ram(ram);
        this.bw = new Bandwidth(bw);
        
        this.setGpuGddramProvisioner(new GpuResourceProvisionerSimple());
        this.setGpuBwProvisioner(new GpuResourceProvisionerSimple());

        this.setVGpuScheduler(new VGpuSchedulerSpaceShared());
        this.setGpuCoreList(coreList);
        this.setFailed(false);
        this.shutdownTime = -1;
        this.setVideocard(Videocard.NULL);

        this.onUpdateProcessingListeners = new HashSet<>();
        this.onStartupListeners = new ArrayList<>();
        this.onShutdownListeners = new ArrayList<>();
        //this.cpuUtilizationStats = GpuResourceStats.NULL;

        this.resources = new ArrayList<>();
        this.vgpuCreatedList = new ArrayList<>();
        this.provisioners = new ArrayList<>();
        this.vgpusMigratingIn = new HashSet<>();
        this.vgpusMigratingOut = new HashSet<>();
        //this.powerModel = PowerModelGpu.NULL;
        this.stateHistory = new LinkedList<>();
        this.activateOnVideocardStartup = activate;
        
		this.ram = new Ram(ram);
		this.bw = new Bandwidth(bw);
		
		//this.setGpuGddramProvisioner(new GpuResourceProvisionerSimple());
        this.setGpuBwProvisioner(new GpuResourceProvisionerSimple());
        this.setGpuCoreList(coreList);
	}
	
	public static long getDefaultGddramCapacity () {
        return defaultGddramCapacity;
    }

    public static void setDefaultGddramCapacity (final long defaultCapacity) {
    	if(defaultCapacity <= 0)
    		throw new IllegalArgumentException("Capacity must be greater than zero");
    	
    	defaultGddramCapacity = defaultCapacity;
    }

    public static long getDefaultBwCapacity () {
        return defaultBwCapacity;
    }

    public static void setDefaultBwCapacity (final long defaultCapacity) {
    	if(defaultCapacity <= 0)
    		throw new IllegalArgumentException("Capacity must be greater than zero");
        defaultBwCapacity = defaultCapacity;
    }
	
	@Override 
	public final void setId (long id) {
		this.id = id;
	}
	@Override 
	public long getId () {
        return id;
    }
	
	public void setType (String type) {
		this.type = type;
	}
	
	public String getType () {
		return type;
	}
	
	public void setGpuCoreList (final List<Pe> gpuCoreList) {
		if(requireNonNull(gpuCoreList).isEmpty()){
            throw new IllegalArgumentException("The CORE list for a Gpu cannot be empty");
        }
	    checkSimulationIsRunningAndAttemptedToChangeGpu("List of PE");

		this.gpuCoreList = gpuCoreList;
		
		long coreId = Math.max(gpuCoreList.get(gpuCoreList.size()-1).getId(), -1);
	    for(final Pe core: gpuCoreList){
	        if(core.getId() < 0) {
	            core.setId(++coreId);
	        }
	        core.setStatus(Pe.Status.FREE);
	    }

	    failedCoresNumber = 0;
	    busyCoresNumber = 0;
	    freeCoresNumber = gpuCoreList.size();
	    workingCoresNumber = freeCoresNumber;
	}

	@Override 
	public List<Pe> getGpuCoreList () {
		return gpuCoreList;
    }
	
	
	@Override 
	public Gpu setGpuGddramProvisioner (GpuResourceProvisioner gpuGddramProvisioner) {
        checkSimulationIsRunningAndAttemptedToChangeGpu("GDDRAM");
		this.gpuGddramProvisioner = requireNonNull(gpuGddramProvisioner);
        this.gpuGddramProvisioner.setResources(ram, vgpu -> ((CustomVGpuSimple)vgpu).getGddram());
        return this;
	}
	
	@Override 
	public GpuResourceProvisioner getGpuGddramProvisioner () {
        return gpuGddramProvisioner;
    }
        
    @Override 
    public Gpu setGpuBwProvisioner (GpuResourceProvisioner gpuBwProvisioner) {
        checkSimulationIsRunningAndAttemptedToChangeGpu("BW");
    	this.gpuBwProvisioner = requireNonNull(gpuBwProvisioner);
        this.gpuBwProvisioner.setResources(bw, vgpu -> ((CustomVGpuSimple)vgpu).getBw());
        //must add set resource in gpurespro //has to
    	return this;
    }
    
    @Override 
    public GpuResourceProvisioner getGpuBwProvisioner () {
        return gpuBwProvisioner;
    }
    
    @SuppressWarnings("ForLoopReplaceableByForEach")
    @Override
    public double updateProcessing (final double currentTime) {
        if(vgpuList.isEmpty() && isIdleEnough(idleShutdownDeadline))
            setActive(false);
        

        double nextSimulationDelay = Double.MAX_VALUE;

        for (int i = 0; i < vgpuList.size(); i++) {
            nextSimulationDelay = updateVGpuProcessing(vgpuList.get(i), currentTime, 
            		nextSimulationDelay);
        }

        notifyOnUpdateProcessingListeners(currentTime);
        //cpuUtilizationStats.add(currentTime);
        addStateHistory(currentTime);
        if (!vgpuList.isEmpty()) 
            lastBusyTime = currentTime;

        return nextSimulationDelay;
    }
    
    public boolean isIdleEnough (final double time) {
        if(time < 0) {
            return false;
        }

        return getIdleInterval() >= time;
    }

    public double getIdleInterval() {
        return getSimulation().clock() - getLastBusyTime();
    }

    protected double updateVGpuProcessing (final CustomVGpu vgpu, final double currentTime, 
    		final double nextSimulationDelay) {
        final double delay = vgpu.updateGpuTaskProcessing(currentTime, 
        		vgpuScheduler.getAllocatedMips(vgpu));
        return delay > 0 ? Math.min(delay, nextSimulationDelay) : nextSimulationDelay;
    }

    private void notifyOnUpdateProcessingListeners(final double nextSimulationTime) {
        onUpdateProcessingListeners.forEach(l -> l.update(GpuUpdatesVgpusProcessingEventInfo.of(l,
        		this, nextSimulationTime)));
    }

    @Override
    public GpuSuitability createVGpu (final CustomVGpu vgpu) {
        final GpuSuitability suitability = createVGpuInternal(vgpu);
        if(suitability.fully()) {
            addVGpuToCreatedList(vgpu);
            vgpu.setGpu(this);
            vgpu.setCreated(true);
            vgpu.setStartTime(getSimulation().clock());
        }

        return suitability;
    }

    @Override
    public GpuSuitability createTemporaryVGpu (final CustomVGpu vgpu) {
        return createVGpuInternal(vgpu);
    }

    private GpuSuitability createVGpuInternal (final CustomVGpu vgpu) {
        /*if(vm instanceof VmGroup){
            return new HostSuitability("Just internal VMs inside a VmGroup can be created, not the VmGroup itself.");
        }*/

        final GpuSuitability suitability = allocateResourcesForVGpu(vgpu, false);
        if(suitability.fully()){
            vgpuList.add(vgpu);
        }

        return suitability;
    }
    
    private GpuSuitability allocateResourcesForVGpu (final CustomVGpu vgpu, 
    		final boolean inMigration) {
        final GpuSuitability suitability = isSuitableForVGpu(vgpu, inMigration, true);
        if(!suitability.fully()) {
            return suitability;
        }

        if(inMigration) {
            vgpusMigratingIn.add(vgpu);
        }
        vgpu.setInMigration(inMigration);
        allocateResourcesForVGpu (vgpu);

        return suitability;
    }

    private void allocateResourcesForVGpu (final CustomVGpu vgpu) {
        gpuGddramProvisioner.allocateResourceForVGpu(vgpu, vgpu.getCurrentRequestedGddram());
        gpuBwProvisioner.allocateResourceForVGpu(vgpu, vgpu.getCurrentRequestedBw());
        //disk.getStorage().allocateResource(vm.getStorage());
        vgpuScheduler.allocateCoresForVGpu(vgpu, vgpu.getCurrentRequestedMips());
    }
    
    private void logAllocationError (final boolean showFailureLog, final CustomVGpu vgpu,
            final boolean inMigration, final String resourceUnit, final Resource gpuResource, 
            final Resource vgpuRequestedResource) {
    	
    	if(!showFailureLog){
        	return;
    	}

    	final String migration = inMigration ? "VM Migration" : "VM Creation";
    	final String msg = gpuResource.getAvailableResource() > 0 ?
    					"just "+gpuResource.getAvailableResource()+" " + resourceUnit :
    					"no amount";
    	LOGGER.error(
    			"{}: {}: [{}] Allocation of {} to {} failed due to lack of {}. Required {} but there is {} available.",
    			getSimulation().clockStr(), getClass().getSimpleName(), migration, vgpu, this,
                gpuResource.getClass().getSimpleName(), vgpuRequestedResource.getCapacity(), msg);
	}
    
    @Override
    public void reallocateMigratingInVGpus () {
        for (final CustomVGpu vgpu : getVGpusMigratingIn()) {
            if (!vgpuList.contains(vgpu)) {
                vgpuList.add(vgpu);
            }

            allocateResourcesForVGpu(vgpu);
        }
    }

    @Override
    public boolean isSuitableForVGpu (final CustomVGpu vgpu) {
        return getSuitabilityFor(vgpu).fully();
    }
    
    private GpuSuitability isSuitableForVGpu (final CustomVGpu vgpu, final boolean inMigration, 
    		final boolean showFailureLog) {
        final GpuSuitability suitability = new GpuSuitability();

        /*suitability.setForStorage(disk.isAmountAvailable(vm.getStorage()));
        if (!suitability.forStorage()) {
            logAllocationError(showFailureLog, vm, inMigration, "MB", this.getStorage(), vm.getStorage());
            if(lazySuitabilityEvaluation)
                return suitability;
        }*/

        suitability.setForGddram(gpuGddramProvisioner.isSuitableForVGpu(vgpu, vgpu.getGddram()));
        if (!suitability.forGddram()) {
            logAllocationError(showFailureLog, vgpu, inMigration, "MB", this.getGddram(), 
            		vgpu.getGddram());
            if(lazySuitabilityEvaluation)
                return suitability;
        }

        suitability.setForBw(gpuBwProvisioner.isSuitableForVGpu(vgpu, vgpu.getBw()));
        if (!suitability.forBw()) {
            logAllocationError(showFailureLog, vgpu, inMigration, "Mbps", this.getBw(), vgpu.getBw());
            if(lazySuitabilityEvaluation)
                return suitability;
        }

        return suitability.setForCores(vgpuScheduler.isSuitableForVGpu(vgpu));
    }

    @Override
    public GpuSuitability getSuitabilityFor (final CustomVGpu vgpu) {
        return isSuitableForVGpu(vgpu, false, false);
    }

    @Override
    public boolean isActive () {
        return this.active;
    }

    @Override
    public boolean hasEverStarted () {
        return this.firstStartTime > -1;
    }

    @Override
    public final Gpu setActive (final boolean activate) {
        if(!activate) 
            activateOnVideocardStartup = false;

        final double delay = activate ? powerModel.getStartupDelay() : powerModel.getShutDownDelay();
        if(this.active == activate || delay > 0 && activationChangeInProgress){
            return this;
        }

        if(isFailed() && activate){
            throw new IllegalStateException("The Gpu is failed and cannot be activated.");
        }

        if (delay == 0) {
           processActivation(activate);
           return this;
        }

        /*If the simulation is not running and there is a startup delay,
        * when the datacenter is started up, it will request such a Host activation. */
        if(!getSimulation().isRunning()){
            return this;
        }

        final CloudSimTag tag = activate ? CloudSimTag.GPU_POWER_ON : CloudSimTag.GPU_POWER_OFF;
        final String msg = (activate ? "on" : "off") + " (expected time: {} seconds).";
        LOGGER.info("{}: {} is being powered " + msg, getSimulation().clockStr(), this, delay);
        datacenter.schedule(delay, tag, this);
        activationChangeInProgress = true;

        return this;
    }
    
    public void processActivation (final boolean activate) {
        final boolean wasActive = this.active;
        if(activate) {
            setStartTime(getSimulation().clock());
            powerModel.addStartupTotals();
        } else {
            setShutdownTime(getSimulation().clock());
            powerModel.addShutDownTotals();
        }

        this.active = activate;
        ((DatacenterSimple) datacenter).updateActiveHostsNumber(this);
        activationChangeInProgress = false;
        notifyStartupOrShutdown(activate, wasActive);
    }
    
    private void notifyStartupOrShutdown (final boolean activate, final boolean wasActive) {
        if(getSimulation() == null || !getSimulation().isRunning() ) 
            return;
   
        if(activate && !wasActive){
            LOGGER.info("{}: {} is powered on.", getSimulation().clockStr(), this);
            updateOnStartupListeners();
        }
        else if(!activate && wasActive){
            final String reason = isIdleEnough(idleShutdownDeadline) ? " after becoming idle" : "";
            LOGGER.info("{}: {} is powered off{}.", getSimulation().clockStr(), this, reason);
            updateOnShutdownListeners();
        }
    }

    private void updateOnShutdownListeners () {
        for (int i = 0; i < onShutdownListeners.size(); i++) {
            final EventListener<GpuEventInfo> listener = onShutdownListeners.get(i);
            listener.update(GpuEventInfo.of(listener, this, getSimulation().clock()));
        }
    }

    private void updateOnStartupListeners () {
        for (int i = 0; i < onStartupListeners.size(); i++) {
            final EventListener<GpuEventInfo> listener = onStartupListeners.get(i);
            listener.update(GpuEventInfo.of(listener, this, getSimulation().clock()));
        }
    }
    
    @Override
    public void destroyVGpu (final CustomVGpu vgpu) {
        if(!vgpu.isCreated()){
            return;
        }

        destroyVGpuInternal(vgpu);
        vgpu.setStopTime(getSimulation().clock());
        vgpu.notifyOnGpuDeallocationListeners(this);
    }

    @Override
    public void destroyTemporaryVGpu (final CustomVGpu vgpu) {
        destroyVGpuInternal(vgpu);
    }

    private void destroyVGpuInternal(final CustomVGpu vgpu) {
        deallocateResourcesOfVGpu(requireNonNull(vgpu));
        vgpuList.remove(vgpu);
        vgpu.getBroker().getVmExecList().remove(vgpu);
    }
    
    protected void deallocateResourcesOfVGpu (final CustomVGpu vgpu) {
        vgpu.setCreated(false);
        gpuGddramProvisioner.deallocateResourceForVGpu(vgpu);
        gpuBwProvisioner.deallocateResourceForVGpu(vgpu);
        vgpuScheduler.deallocateCoresFromVGpu(vgpu);
        //disk.getStorage().deallocateResource(vm.getStorage());
    }

    @Override
    public void destroyAllVGpus () {
        final PeProvisioner peProvisioner = getGpuCoreList().get(0).getPeProvisioner();
        for (final CustomVGpu vgpu : vgpuList) {
            gpuGddramProvisioner.deallocateResourceForVGpu(vgpu);
            gpuBwProvisioner.deallocateResourceForVGpu(vgpu);
            peProvisioner.deallocateResourceForVm(vgpu);
            vgpu.setCreated(false);
            //disk.getStorage().deallocateResource(vm.getStorage());
        }

        vgpuList.clear();
    }
    
    @Override
    public Gpu addOnStartupListener (final EventListener<GpuEventInfo> listener) {
        if(EventListener.NULL.equals(listener))
            return this;
        
        onStartupListeners.add(requireNonNull(listener));
        return this;
    }

    @Override
    public boolean removeOnStartupListener (final EventListener<GpuEventInfo> listener) {
        return onStartupListeners.remove(listener);
    }

    @Override
    public Gpu addOnShutdownListener (final EventListener<GpuEventInfo> listener) {
        if(EventListener.NULL.equals(listener)){
            return this;
        }

        onShutdownListeners.add(requireNonNull(listener));
        return this;
    }
    
    @Override
    public boolean removeOnShutdownListener (final EventListener<GpuEventInfo> listener) {
        return onShutdownListeners.remove(listener);
    }

    //@Override
    public long getNumberOfCores () {
        return gpuCoreList.size();
    }
    
    protected MipsShare getAllocatedMipsForVGpu (final CustomVGpu vgpu) {
        return vgpuScheduler.getAllocatedMips(vgpu);
    }

    //@Override
    public double getMips () {
        return gpuCoreList.stream().mapToDouble(Pe::getCapacity).findFirst().orElse(0);
    }

    @Override
    public double getTotalMipsCapacity () {
        return gpuCoreList.stream()
                     .filter(Pe::isWorking)
                     .mapToDouble(Pe::getCapacity)
                     .sum();
    }

    @Override
    public double getTotalAvailableMips () {
        return vgpuScheduler.getTotalAvailableMips();
    }

    @Override
    public double getTotalAllocatedMips() {
        return getTotalMipsCapacity() - getTotalAvailableMips();
    }

    @Override
    public double getTotalAllocatedMipsForVGpu (final CustomVGpu vgpu) {
        return vgpuScheduler.getTotalAllocatedMipsForVGpu(vgpu);
    }

    @Override
    public Resource getBw () {
        return gpuBwProvisioner.getPGpuResource();
    }

    @Override
    public Resource getGddram() {
        return gpuGddramProvisioner.getPGpuResource();
    }

    /*@Override
    public FileStorage getStorage() {
        return disk;
    }*/

    private void checkSimulationIsRunningAndAttemptedToChangeGpu (final String resourceName) {
        if(getSimulation().isRunning()){
            final String msg = "It is not allowed to change a Gpu's %s after the simulation started.";
            throw new IllegalStateException(String.format(msg, resourceName));
        }
    }

    @Override
    public VGpuScheduler getVGpuScheduler() {
        return vgpuScheduler;
    }

    @Override
    public final Gpu setVGpuScheduler(final VGpuScheduler vgpuScheduler) {
        this.vgpuScheduler = requireNonNull(vgpuScheduler);
        vgpuScheduler.setGpu(this);
        return this;
    }

    @Override
    public double getStartTime () {
        return startTime;
    }

    @Override
    public double getFirstStartTime (){
        return firstStartTime;
    }

    @Override
    public Gpu setStartTime (final double startTime) {
        if(startTime < 0){
            throw new IllegalArgumentException("Gpu start time cannot be negative");
        }

        this.startTime = Math.floor(startTime);
        if(firstStartTime == -1){
            firstStartTime = this.startTime;
        }

        this.lastBusyTime = startTime;

        //If the Host is being activated or re-activated, the shutdown time is reset
        this.shutdownTime = -1;
        return this;
    }

    @Override
    public double getShutdownTime() {
        return shutdownTime;
    }

    @Override
    public void setShutdownTime(final double shutdownTime) {
        if(shutdownTime < 0){
            throw new IllegalArgumentException("Gpu shutdown time cannot be negative");
        }

        this.shutdownTime = Math.floor(shutdownTime);
        this.totalUpTime += getUpTime();
    }

    /*@Override
    public double getUpTime() {
        return active ? simulation.clock() - startTime : shutdownTime - startTime;
    }

    @Override
    public double getTotalUpTime() {
        return totalUpTime + (active ? getUpTime() : 0);
    }

    @Override
    public double getUpTimeHours() {
        return TimeUtil.secondsToHours(getUpTime());
    }

    @Override
    public double getTotalUpTimeHours() {
        return TimeUtil.secondsToHours(getTotalUpTime());
    }*/

    @Override
    public double getIdleShutdownDeadline () {
        return idleShutdownDeadline;
    }

    @Override
    public Gpu setIdleShutdownDeadline (final double deadline) {
        this.idleShutdownDeadline = deadline;
        return this;
    }
    
    @Override
    public <T extends CustomVGpu> List<T> getVGpuList() {
        return (List<T>) vgpuList;
    }

    @Override
    public <T extends CustomVGpu> List<T> getVGpuCreatedList() {
        return (List<T>) Collections.unmodifiableList(vgpuCreatedList);
    }

    protected void addVGpuToList (final CustomVGpu vgpu){
        vgpuList.add(requireNonNull(vgpu));
    }

    protected void addVGpuToCreatedList (final CustomVGpu vgpu){
        vgpuCreatedList.add(requireNonNull(vgpu));
    }

    @Override
    public boolean isFailed() {
        return failed;
    }

    @Override
    public final boolean setFailed (final boolean failed) {
        this.failed = failed;
        final Pe.Status newStatus = failed ? Pe.Status.FAILED : Pe.Status.FREE;
        setCoreStatus(gpuCoreList, newStatus);

        if(failed && this.active){
            this.active = false;
        }

        return true;
    }

    public final void setCoreStatus (final List<Pe> coreList, final Pe.Status newStatus){
        /*For performance reasons, stores the number of free and failed PEs
        instead of iterating over the PE list every time to find out.*/
        for (final Pe core : coreList) {
            updateCoreStatus(core, newStatus);
        }
    }

    private void updateCoreStatus (final Pe core, final Pe.Status newStatus) {
        if(core.getStatus() != newStatus) {
            updateCoreStatusCount(core.getStatus(), false);
            updateCoreStatusCount(newStatus, true);
            core.setStatus(newStatus);
        }
    }

    private void updateCoreStatusCount (final Pe.Status status, final boolean isIncrement) {
        final int inc = isIncrement ? 1 : -1;
        switch (status) {
            case FAILED:
            	incFailedCoresNumber(inc);
            case FREE:
            	incFreeCoresNumber(inc);
            case BUSY:
            	incBusyCoresNumber(inc);
        }
    }

    protected void incFailedCoresNumber (final int inc) {
        this.failedCoresNumber += inc;
        workingCoresNumber -= inc;
    }

    protected void incFreeCoresNumber (final int inc) {
        this.freeCoresNumber += inc;
    }

    protected void incBusyCoresNumber(final int inc) {
        this.busyCoresNumber += inc;
    }

    @Override
    public <T extends CustomVGpu> Set<T> getVGpusMigratingIn() {
        return (Set<T>)vgpusMigratingIn;
    }

    @Override
    public boolean hasMigratingVGpus () {
        return !(vgpusMigratingIn.isEmpty() && vgpusMigratingOut.isEmpty());
    }
    
    @Override
    public boolean addMigratingInVGpu (final CustomVGpu vgpu) {
        /* TODO: Instead of keeping a list of VMs which are migrating into a Host,
        *  which requires searching in such a list every time a VM is requested to be migrated
        *  to that Host (to check if it isn't migrating to that same host already),
        *  we can add a migratingHost attribute to Vm, so that the worst time complexity
        *  will change from O(N) to a constant time O(1). */
        if (vgpusMigratingIn.contains(vgpu)) {
            return false;
        }

        if(!allocateResourcesForVGpu(vgpu, true).fully()){
            return false;
        }

        ((CustomVGpuSimple)vgpu).updateMigrationStartListeners(this);

        updateProcessing(getSimulation().clock());
        vgpu.getGpu().updateProcessing(getSimulation().clock());

        return true;
    }
    
    @Override
    public void removeMigratingInVGpu (final CustomVGpu vgpu) {
        vgpusMigratingIn.remove(vgpu);
        vgpuList.remove(vgpu);
        vgpu.setInMigration(false);
    }

    @Override
    public Set<CustomVGpu> getVGpusMigratingOut () {
        return Collections.unmodifiableSet(vgpusMigratingOut);
    }

    @Override
    public boolean addVGpuMigratingOut (final CustomVGpu vgpu) {
        return this.vgpusMigratingOut.add(vgpu);
    }

    @Override
    public boolean removeVGpuMigratingOut(final CustomVGpu vgpu) {
        return this.vgpusMigratingOut.remove(vgpu);
    }

    @Override
    public Videocard getVideocard () {
        return videocard;
    }

    @Override
    public final void setVideocard (final Videocard videocard) {
        if(!Videocard.NULL.equals(this.videocard)) 
            checkSimulationIsRunningAndAttemptedToChangeGpu("Videocard");

        this.videocard = videocard;
    }

    @Override
    public String toString() {
        final String vc =
        		videocard == null || Videocard.NULL.equals(videocard) ? "" :
                String.format("/videocard %d", videocard.getId());
        return String.format("gpu %d%s", getId(), vc);
    }

    @Override
    public boolean removeOnUpdateProcessingListener (
    		final EventListener<GpuUpdatesVgpusProcessingEventInfo> listener) {
        return onUpdateProcessingListeners.remove(listener);
    }

    @Override
    public Gpu addOnUpdateProcessingListener (
    		final EventListener<GpuUpdatesVgpusProcessingEventInfo> listener) {
        if(EventListener.NULL.equals(listener)){
            return this;
        }

        this.onUpdateProcessingListeners.add(requireNonNull(listener));
        return this;
    }

    /*@Override
    public long getAvailableStorage() {
        return disk.getAvailableResource();
    }*/

    @Override
    public int getFreeCoresNumber () {
        return freeCoresNumber;
    }

    @Override
    public int getWorkingCoresNumber () {
        return workingCoresNumber;
    }

    @Override
    public int getBusyCoresNumber () {
        return busyCoresNumber;
    }

    @Override
    public double getBusyCoresPercent() {
        return getBusyCoresNumber() / (double)getNumberOfCores();
    }

    @Override
    public double getBusyCoresPercent (final boolean hundredScale) {
        final double scale = hundredScale ? 100 : 1;
        return getBusyCoresPercent() * scale;
    }

    @Override
    public int getFailedCoresNumber() {
        return failedCoresNumber;
    }

    @Override
    public Simulation getSimulation() {
        return videocard.getSimulation();
    }

    @Override
    public double getLastBusyTime() {
        return lastBusyTime;
    }

    /*@Override
    public final Host setSimulation(final Simulation simulation) {
        this.simulation = simulation;
        return this;
    }*/

    @Override
    public int compareTo (final Gpu other) {
        if(this.equals(requireNonNull(other))) {
            return 0;
        }

        return Long.compare(this.id, other.getId());
    }
    
    @Override
    public boolean equals (final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final GpuSimple that = (GpuSimple) obj;
        return this.getId() == that.getId() && this.getSimulation().equals(that.getSimulation());
    }

    @Override
    public int hashCode () {
        int result = Long.hashCode(id);
        result = 31 * result + getSimulation().hashCode();
        return result;
    }

    @Override
    public List<ResourceManageable> getResources () {
        if(getSimulation().isRunning() && resources.isEmpty()){
            resources = Arrays.asList(ram, bw);
        }

        return Collections.unmodifiableList(resources);
    }

    @Override
    public GpuResourceProvisioner getProvisioner (
    		final Class<? extends ResourceManageable> resourceClass) {
        if(getSimulation().isRunning() && provisioners.isEmpty()){
            provisioners = Arrays.asList(gpuGddramProvisioner, gpuBwProvisioner);
        }

        return provisioners
            .stream()
            .filter(provisioner -> provisioner.getPGpuResource().isSubClassOf(resourceClass))
            .findFirst()
            .orElse(GpuResourceProvisioner.NULL);
    }

    @Override
    public List<Pe> getWorkingCoreList () {
        return getFilteredCoreList(Pe::isWorking);
    }

    @Override
    public List<Pe> getBusyCoreList() {
        return getFilteredCoreList(Pe::isBusy);
    }

    @Override
    public List<Pe> getFreeCoreList() {
        return getFilteredCoreList(Pe::isFree);
    }

    private List<Pe> getFilteredCoreList(final Predicate<Pe> status) {
        return gpuCoreList.stream().filter(status).collect(toList());
    }

    @Override
    public double getGpuCorePercentUtilization() {
        return computeCpuUtilizationPercent(getGpuCoreMipsUtilization());
    }

    @Override
    public double getGpuCorePercentRequested () {
        return computeCpuUtilizationPercent(getGpuCoreMipsRequested());
    }

    private double computeCpuUtilizationPercent(final double mipsUsage){
        final double totalMips = getTotalMipsCapacity();
        if(totalMips == 0){
            return 0;
        }

        final double utilization = mipsUsage / totalMips;
        return utilization > 1 && utilization < 1.01 ? 1 : utilization;
    }

    @Override
    public double getGpuCoreMipsUtilization () {
        return vgpuList.stream().mapToDouble(CustomVGpu::getTotalCpuMipsUtilization).sum();
    }

    private double getGpuCoreMipsRequested () {
        return vgpuList.stream().mapToDouble(Vm::getTotalCpuMipsRequested).sum();
    }

    @Override
    public long getGddramUtilization() {
        return gpuGddramProvisioner.getTotalAllocatedResource();
    }

    @Override
    public long getBwUtilization() {
        return gpuBwProvisioner.getTotalAllocatedResource();
    }

    @Override
    public GpuResourceStats getCpuUtilizationStats() {
        return cpuUtilizationStats;
    }

    @Override
    public void enableUtilizationStats() {
        if (cpuUtilizationStats != null && cpuUtilizationStats != HostResourceStats.NULL) {
            return;
        }

        this.cpuUtilizationStats = new HostResourceStats(this, Host::getCpuPercentUtilization);
        if(vmList.isEmpty()){
            final String host = this.getId() > -1 ? this.toString() : "Host";
            LOGGER.info("Automatically enabling computation of utilization statistics for VMs on {} could not be performed because it doesn't have VMs yet. You need to enable it for each VM created.", host);
        }
        else vmList.forEach(ResourceStatsComputer::enableUtilizationStats);
    }
    
    /*@Override
    public PowerModelHost getPowerModel() {
        return powerModel;
    }

    @Override
    public final void setPowerModel(final PowerModelHost powerModel) {
        requireNonNull(powerModel,
            "powerModel cannot be null. You could provide a " +
            PowerModelHost.class.getSimpleName() + ".NULL instead.");

        if(powerModel.getHost() != null && powerModel.getHost() != Host.NULL && !this.equals(powerModel.getHost())){
            throw new IllegalStateException("The given PowerModel is already assigned to another Host. Each Host must have its own PowerModel instance.");
        }

        this.powerModel = powerModel;
        powerModel.setHost(this);
    }*/
    
    @Override
    public void enableStateHistory() {
        this.stateHistoryEnabled = true;
    }

    @Override
    public void disableStateHistory() {
        this.stateHistoryEnabled = false;
    }

    @Override
    public boolean isStateHistoryEnabled() {
        return this.stateHistoryEnabled;
    }

    @Override
    public List<CustomVGpu> getFinishedVGpus() {
        return getVGpuList().stream()
            .filter(vgpu -> !vgpu.isInMigration())
            .filter(vgpu -> vgpu.getTotalCpuMipsRequested() == 0)
            .collect(toList());
    }
    
    private double addVGpuResourceUseToHistoryIfNotMigratingIn (final CustomVGpu vgpu,
    		final double currentTime) {
        double totalAllocatedMips = getVGpuScheduler().getTotalAllocatedMipsForVGpu(vgpu);
        if (getVGpusMigratingIn().contains(vgpu)) {
            LOGGER.info("{}: {}: {} is migrating in", getSimulation().clockStr(), this, vgpu);
            return totalAllocatedMips;
        }

        final double totalRequestedMips = vgpu.getTotalCpuMipsRequested();
        if (totalAllocatedMips + 0.1 < totalRequestedMips) {
            final String reason = getVGpusMigratingOut().contains(vm) ? "migration overhead" : "capacity unavailability";
            final long notAllocatedMipsByPe = (long)((totalRequestedMips - totalAllocatedMips)/vm.getNumberOfPes());
            LOGGER.warn(
                "{}: {}: {} MIPS not allocated for each one of the {} PEs from {} due to {}.",
                getSimulation().clockStr(), this, notAllocatedMipsByPe, vm.getNumberOfPes(), vm, reason);
        }

        final var entry = new VmStateHistoryEntry(
                           currentTime, totalAllocatedMips, totalRequestedMips,
                           vm.isInMigration() && !getVmsMigratingIn().contains(vm));
        vm.addStateHistoryEntry(entry);

        if (vm.isInMigration()) {
            LOGGER.info("{}: {}: {} is migrating out ", getSimulation().clockStr(), this, vm);
            totalAllocatedMips /= getVmScheduler().getMaxCpuUsagePercentDuringOutMigration();
        }

        return totalAllocatedMips;
    }

    private void addStateHistory(final double currentTime) {
        if(!stateHistoryEnabled){
            return;
        }

        double hostTotalRequestedMips = 0;

        for (final CustomVGpu vgpu : getVGpuList()) {
            final double totalRequestedMips = vgpu.getTotalCpuMipsRequested();
            addVmResourceUseToHistoryIfNotMigratingIn(vgpu, currentTime);
            hostTotalRequestedMips += totalRequestedMips;
        }

        addStateHistoryEntry(currentTime, getCpuMipsUtilization(), hostTotalRequestedMips, active);
    }
    
    private void addStateHistoryEntry (final double time, final double allocatedMips, 
    		final double requestedMips, final boolean isActive) {
    	final var newState = new HostStateHistoryEntry(time, allocatedMips, requestedMips, isActive);
    	if (!stateHistory.isEmpty()) {
    		final HostStateHistoryEntry previousState = stateHistory.get(stateHistory.size() - 1);
        	if (previousState.time() == time) {
        		stateHistory.set(stateHistory.size() - 1, newState);
        		return;
            }
    	}
    	stateHistory.add(newState);
	}

	@Override
	public List<GpuStateHistoryEntry> getStateHistory () {
    	return Collections.unmodifiableList(stateHistory);
	}

	@Override
	public List<CustomVGpu> getMigratableVGpus () {
		return vgpuList.stream().filter(vm -> !vm.isInMigration()).collect(toList());
	}

	@Override
	public boolean isLazySuitabilityEvaluation () {
		return lazySuitabilityEvaluation;
	}

	@Override
	public Gpu setLazySuitabilityEvaluation (final boolean lazySuitabilityEvaluation) {
		this.lazySuitabilityEvaluation = lazySuitabilityEvaluation;
		return this;
	}

	public boolean isActivateOnVideocardStartup () {
		return activateOnVideocardStartup;
	}
}