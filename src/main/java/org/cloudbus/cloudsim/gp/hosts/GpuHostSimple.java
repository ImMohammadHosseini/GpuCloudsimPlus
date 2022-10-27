package org.cloudbus.cloudsim.gp.hosts;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmGroup;
import org.cloudbus.cloudsim.resources.*;
import org.cloudbus.cloudsim.util.TimeUtil;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.util.BytesConversion;
//import org.cloudbus.cloudsim.hosts.HostSuitability;
import org.cloudbus.cloudsim.vms.HostResourceStats;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.vms.VmStateHistoryEntry;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.hosts.HostStateHistoryEntry;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;

import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.HostEventInfo;
import org.cloudsimplus.listeners.HostUpdatesVmsProcessingEventInfo;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.vms.GpuVmSimple;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenter;
import org.cloudbus.cloudsim.gp.videocards.VideocardSimple;
import org.cloudbus.cloudsim.gp.schedulers.gpuvm.GpuVmScheduler;
import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenterSimple;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicy;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicySimple;

import java.util.*;
import java.util.stream.Stream;
import java.util.function.Predicate;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

//  extends HostSimple 
public class GpuHostSimple extends HostSimple  implements GpuHost {
	
	private static long defaultRamCapacity = (long) BytesConversion.gigaToMega(10);
    private static long defaultBwCapacity = 1000;
    private static long defaultStorageCapacity = (long) BytesConversion.gigaToMega(500);
    
	private Videocard videocard;
	
	protected HostResourceStats cpuUtilizationStats;

    private final List<HostStateHistoryEntry> stateHistory;
    private boolean activateOnDatacenterStartup;

    private PowerModelHost powerModel;

    private long id;

    private boolean failed;
    private boolean active;
    private boolean activationChangeInProgress;
    private boolean stateHistoryEnabled;

    private double startTime = -1;
    private double firstStartTime = -1;
    private double shutdownTime;
    private double totalUpTime;
    private double lastBusyTime;
    private double idleShutdownDeadline;

    private final Ram ram;
    private final Bandwidth bw;

    private final HarddriveStorage disk;

    private ResourceProvisioner ramProvisioner;
    private ResourceProvisioner bwProvisioner;
    private VmScheduler vmScheduler;
    private final List<Vm> vmList = new ArrayList<>();
    private List<Pe> peList;
    private final Set<Vm> vmsMigratingIn;

    private final Set<Vm> vmsMigratingOut;

    private GpuDatacenter datacenter;

    private final Set<EventListener<HostUpdatesVmsProcessingEventInfo>> onUpdateProcessingListeners;
    private final List<EventListener<HostEventInfo>> onStartupListeners;
    private final List<EventListener<HostEventInfo>> onShutdownListeners;
    
    private Simulation simulation;

    private List<ResourceManageable> resources;

    private List<ResourceProvisioner> provisioners;
    private final List<Vm> vmCreatedList;

    private int freePesNumber;
    private int busyPesNumber;
    private int workingPesNumber;
    private int failedPesNumber;

    private boolean lazySuitabilityEvaluation;
	
	public GpuHostSimple (final List<Pe> peList, final Videocard videocard) {
        this(peList, videocard, true);
    }
	
    public GpuHostSimple (final List<Pe> peList, final Videocard videocard, final boolean activate) {
    	
        this(defaultRamCapacity, defaultBwCapacity, defaultStorageCapacity, peList, videocard, activate,
        		new VGpuAllocationPolicySimple());
    }

    public GpuHostSimple (final ResourceProvisioner ramProvisioner, 
    		final ResourceProvisioner bwProvisioner, final long storage, final List<Pe> peList,
    		final Videocard videocard, final VGpuAllocationPolicy vgpuAllocationPolicyfinal) {
    	
        this(ramProvisioner.getCapacity(), bwProvisioner.getCapacity(), storage, peList, videocard);
        //setVideocard(videocard);
        setRamProvisioner(ramProvisioner);
        setBwProvisioner(bwProvisioner);
        setPeList(peList);
        this.videocard.setVGpuAllocationPolicy(vgpuAllocationPolicyfinal);
        
    }

    public GpuHostSimple (final long ram, final long bw, final long storage, final List<Pe> peList,
    		final Videocard videocard) {
        this(ram, bw, new HarddriveStorage(storage), peList, videocard);
    }

    public GpuHostSimple ( final long ram, final long bw, final HarddriveStorage storage, 
    		final List<Pe> peList, final Videocard videocard) {
    	super(ram, bw, storage, peList);
        //this(ram, bw, storage, peList, true);
        setVideocard (videocard);
    }

    public GpuHostSimple (final long ram, final long bw, final long storage,
    		final List<Pe> peList, final Videocard videocard, boolean activate, 
    		final VGpuAllocationPolicy vgpuAllocationPolicyfinal) {
    	super(ram, bw, storage, peList, activate);
        //this(ram, bw, new HarddriveStorage(storage), peList, activate);
        setVideocard (videocard);
        this.videocard.setVGpuAllocationPolicy(vgpuAllocationPolicyfinal);
    }

    //private GpuHostSimple(final long ram, final long bw, final HarddriveStorage storage,
    //        final List<Pe> peList, final boolean activate) {
    //	super(ram, bw, storage, peList, activate);
    	/*this.setId(-1);
    	this.setSimulation(Simulation.NULL);
    	this.idleShutdownDeadline = DEF_IDLE_SHUTDOWN_DEADLINE;
    	this.lazySuitabilityEvaluation = true;

     	this.ram = new Ram(ram);
    	this.bw = new Bandwidth(bw);
      	this.disk = requireNonNull(storage);
       	this.setRamProvisioner(new ResourceProvisionerSimple());
    	this.setBwProvisioner(new ResourceProvisionerSimple());

    	this.setVmScheduler(new VmSchedulerSpaceShared());
     	this.setPeList(peList);
     	this.setFailed(false);
     	this.shutdownTime = -1;
       	this.setDatacenter(GpuDatacenter.NULL);

     	this.onUpdateProcessingListeners = new HashSet<>();
      	this.onStartupListeners = new ArrayList<>();
    	this.onShutdownListeners = new ArrayList<>();
       	this.cpuUtilizationStats = HostResourceStats.NULL;

       	this.resources = new ArrayList<>();
      	this.vmCreatedList = new ArrayList<>();
      	this.provisioners = new ArrayList<>();
       	this.vmsMigratingIn = new HashSet<>();
      	this.vmsMigratingOut = new HashSet<>();
      	this.powerModel = PowerModelHost.NULL;
      	this.stateHistory = new LinkedList<>();
       	this.activateOnDatacenterStartup = activate;*/
	//}

    public static long getDefaultRamCapacity() {
        return defaultRamCapacity;
    }

    public static void setDefaultRamCapacity(final long defaultCapacity) {
        AbstractMachine.validateCapacity(defaultCapacity);
        defaultRamCapacity = defaultCapacity;
    }

    public static long getDefaultBwCapacity() {
        return defaultBwCapacity;
    }

    public static void setDefaultBwCapacity(final long defaultCapacity) {
        AbstractMachine.validateCapacity(defaultCapacity);
        defaultBwCapacity = defaultCapacity;
    }

    public static long getDefaultStorageCapacity() {
        return defaultStorageCapacity;
    }

    public static void setDefaultStorageCapacity(final long defaultCapacity) {
        AbstractMachine.validateCapacity(defaultCapacity);
        defaultStorageCapacity = defaultCapacity;
    }
    
    /*@Override
    public GpuHost setActive (final boolean activate) {
        if(!activate) {
            activateOnGpuDatacenterStartup = false;
        }

        final double delay = activate ? powerModel.getStartupDelay() : powerModel.getShutDownDelay();
        if(this.active == activate || delay > 0 && activationChangeInProgress){
            return this;
        }

        if(isFailed() && activate){
            throw new IllegalStateException("The Host is failed and cannot be activated.");
        }

        if (delay == 0) {
           //If there is no delay, start up or shutdown the Host right away.
           processActivation(activate);
           return this;
        }

        //If the simulation is not running and there is a startup delay,
        // when the datacenter is started up, it will request such a Host activation. 
        if(!simulation.isRunning()){
            return this;
        }

        final CloudSimTag tag = activate ? CloudSimTag.HOST_POWER_ON : CloudSimTag.HOST_POWER_OFF;
        final String msg = (activate ? "on" : "off") + " (expected time: {} seconds).";
        LOGGER.info("{}: {} is being powered " + msg, getSimulation().clockStr(), this, delay);
        datacenter.schedule(delay, tag, this);
        activationChangeInProgress = true;

        return this;
    }*/

	@Override
	public Videocard getVideocard () {
		return videocard;
	}
	
	@Override
	public boolean hasVideocard () {
		return getVideocard() != null;
	}
	
	@Override
	public GpuHost setVideocard (Videocard videocard) {
		this.videocard = videocard;
		if (!videocard.hasGpuHost())
			videocard.setHost(this);
		setUpVideocardSimulation ();
		return this;
	}
	
	protected void setUpVideocardSimulation () {
		this.videocard.setSimulation (this.getSimulation());
	}
	
	/*@Override
    public void processActivation(final boolean activate) {
        super.processActivation(activate);
        videocard.gpusProcessActivation(activate);
        
    }*/
	
    @SuppressWarnings("ForLoopReplaceableByForEach")
    @Override
    public double updateProcessing(final double currentTime) {
        if(vmList.isEmpty() && isIdleEnough(idleShutdownDeadline)){
            setActive(false);
        }

        double nextSimulationDelay = Double.MAX_VALUE;

        /* Uses an indexed for to avoid ConcurrentModificationException,
         * e.g., in cases when Vm is destroyed during simulation execution.*/
        for (int i = 0; i < vmList.size(); i++) {
            nextSimulationDelay = updateVmProcessing(vmList.get(i), currentTime, nextSimulationDelay);
        }

        notifyOnUpdateProcessingListeners(currentTime);
        cpuUtilizationStats.add(currentTime);
        addStateHistory(currentTime);
        if (!vmList.isEmpty()) {
            lastBusyTime = currentTime;
        }

        return nextSimulationDelay;
    }

    protected double updateVmProcessing(final Vm vm, final double currentTime, final double nextSimulationDelay) {
        final double delay = vm.updateProcessing(currentTime, vmScheduler.getAllocatedMips(vm));
        return delay > 0 ? Math.min(delay, nextSimulationDelay) : nextSimulationDelay;
    }

    private void notifyOnUpdateProcessingListeners(final double nextSimulationTime) {
        onUpdateProcessingListeners.forEach(l -> l.update(HostUpdatesVmsProcessingEventInfo.of(l,this, nextSimulationTime)));
    }

    /*@Override
    public HostSuitability createVm(final Vm vm) {
        final HostSuitability suitability = createVmInternal(vm);
        if(suitability.fully()) {
            addVmToCreatedList(vm);
            vm.setHost(this);
            vm.setCreated(true);
            vm.setStartTime(getSimulation().clock());
        }

        return suitability;
    }*/

    /*@Override
    public HostSuitability createTemporaryVm(final Vm vm) {
        return createVmInternal(vm);
    }*/

    /*@Override
    public HostSuitability getSuitabilityFor(final Vm vm) {
    	//return super.getSuitabilityFor(vm);
        return isSuitableForVm(vm, false, false);
    }*/

    /*private HostSuitability createVmInternal(final Vm vm) {
        if(vm instanceof VmGroup){
            return new HostSuitability("Just internal VMs inside a VmGroup can be created, not the VmGroup itself.");
        }

        final HostSuitability suitability = allocateResourcesForVm(vm, false);
        if(suitability.fully()){
            vmList.add(vm);
        }

        return suitability;
    }*/

    /*private HostSuitability allocateResourcesForVm(final Vm vm, final boolean inMigration){
        final HostSuitability suitability = isSuitableForVm(vm, inMigration, true);
        if(!suitability.fully()) {
            return suitability;
        }

        if(inMigration) {
            vmsMigratingIn.add(vm);
        }
        vm.setInMigration(inMigration);
        allocateResourcesForVm(vm);

        return suitability;
    }*/

    private void allocateResourcesForVm(final Vm vm) {
        ramProvisioner.allocateResourceForVm(vm, vm.getCurrentRequestedRam());
        bwProvisioner.allocateResourceForVm(vm, vm.getCurrentRequestedBw());
        disk.getStorage().allocateResource(vm.getStorage());
        vmScheduler.allocatePesForVm(vm, vm.getCurrentRequestedMips());
    }

    private void logAllocationError(
        final boolean showFailureLog, final Vm vm,
        final boolean inMigration, final String resourceUnit,
        final Resource pmResource, final Resource vmRequestedResource)
    {
        if(!showFailureLog){
            return;
        }

        final var migration = inMigration ? "VM Migration" : "VM Creation";
        final var msg = pmResource.getAvailableResource() > 0 ?
                            "just "+pmResource.getAvailableResource()+" " + resourceUnit :
                            "no amount";
        LOGGER.error(
            "{}: {}: [{}] Allocation of {} to {} failed due to lack of {}. Required {} but there is {} available.",
            simulation.clockStr(), getClass().getSimpleName(), migration, vm, this,
            pmResource.getClass().getSimpleName(), vmRequestedResource.getCapacity(), msg);
    }

    @Override
    public void reallocateMigratingInVms() {
        for (final Vm vm : getVmsMigratingIn()) {
            if (!vmList.contains(vm)) {
                vmList.add(vm);
            }

            allocateResourcesForVm(vm);
        }
    }

    @Override
    public boolean isSuitableForVm(final Vm vm) {
        return getSuitabilityFor(vm).fully();
    }

    /*private HostSuitability isSuitableForVm(final Vm vm, final boolean inMigration, final boolean showFailureLog) {
        final var suitability = new HostSuitability();

        suitability.setForStorage(disk.isAmountAvailable(vm.getStorage()));
        if (!suitability.forStorage()) {
            logAllocationError(showFailureLog, vm, inMigration, "MB", this.getStorage(), vm.getStorage());
            if(lazySuitabilityEvaluation)
                return suitability;
        }

        suitability.setForRam(ramProvisioner.isSuitableForVm(vm, vm.getRam()));
        if (!suitability.forRam()) {
            logAllocationError(showFailureLog, vm, inMigration, "MB", this.getRam(), vm.getRam());
            if(lazySuitabilityEvaluation)
                return suitability;
        }

        suitability.setForBw(bwProvisioner.isSuitableForVm(vm, vm.getBw()));
        if (!suitability.forBw()) {
            logAllocationError(showFailureLog, vm, inMigration, "Mbps", this.getBw(), vm.getBw());
            if(lazySuitabilityEvaluation)
                return suitability;
        }

        return suitability.setForPes(vmScheduler.isSuitableForVm(vm));
    }*/

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public boolean hasEverStarted() {
        return this.firstStartTime > -1;
    }

    /*@Override
    public final GpuHost setActive(final boolean activate) {
        if(!activate) {
            activateOnDatacenterStartup = false;
        }

        final double delay = activate ? powerModel.getStartupDelay() : powerModel.getShutDownDelay();
        if(this.active == activate || delay > 0 && activationChangeInProgress){
            return this;
        }

        if(isFailed() && activate){
            throw new IllegalStateException("The Host is failed and cannot be activated.");
        }

        if (delay == 0) {
           //If there is no delay, start up or shutdown the Host right away.
           processActivation(activate);
           return this;
        }

        /*If the simulation is not running and there is a startup delay,
        * when the datacenter is started up, it will request such a Host activation. 
        if(!simulation.isRunning()){
            return this;
        }

        final CloudSimTag tag = activate ? CloudSimTag.HOST_POWER_ON : CloudSimTag.HOST_POWER_OFF;
        final String msg = (activate ? "on" : "off") + " (expected time: {} seconds).";
        LOGGER.info("{}: {} is being powered " + msg, getSimulation().clockStr(), this, delay);
        datacenter.schedule(delay, tag, this);
        activationChangeInProgress = true;

        return this;
    }*/

    public void processActivation(final boolean activate) {
        final boolean wasActive = this.active;
        if(activate) {
            setStartTime(getSimulation().clock());
            powerModel.addStartupTotals();
        } else {
            setShutdownTime(getSimulation().clock());
            powerModel.addShutDownTotals();
        }

        this.active = activate;
        ((GpuDatacenterSimple) datacenter).updateActiveGpuHostsNumber(this);
        activationChangeInProgress = false;
        notifyStartupOrShutdown(activate, wasActive);
        
        videocard.gpusProcessActivation(activate);
    }

    private void notifyStartupOrShutdown(final boolean activate, final boolean wasActive) {
        if(simulation == null || !simulation.isRunning() ) {
            return;
        }

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

    private void updateOnShutdownListeners() {
        for (int i = 0; i < onShutdownListeners.size(); i++) {
            final var listener = onShutdownListeners.get(i);
            listener.update(HostEventInfo.of(listener, this, simulation.clock()));
        }
    }

    private void updateOnStartupListeners() {
        for (int i = 0; i < onStartupListeners.size(); i++) {
            final var listener = onStartupListeners.get(i);
            listener.update(HostEventInfo.of(listener, this, simulation.clock()));
        }
    }

    @Override
    public void destroyVm(final Vm vm) {
        if(!vm.isCreated()){
            return;
        }

        destroyVmInternal(vm);
        vm.setStopTime(getSimulation().clock());
        vm.notifyOnHostDeallocationListeners(this);
    }

    @Override
    public void destroyTemporaryVm(final Vm vm) {
        destroyVmInternal(vm);
    }

    private void destroyVmInternal(final Vm vm) {
        deallocateResourcesOfVm(requireNonNull(vm));
        vmList.remove(vm);
        vm.getBroker().getVmExecList().remove(vm);
    }

    protected void deallocateResourcesOfVm(final Vm vm) {
        vm.setCreated(false);
        ramProvisioner.deallocateResourceForVm(vm);
        bwProvisioner.deallocateResourceForVm(vm);
        vmScheduler.deallocatePesFromVm(vm);
        disk.getStorage().deallocateResource(vm.getStorage());
    }

    @Override
    public void destroyAllVms() {
        final PeProvisioner peProvisioner = getPeList().get(0).getPeProvisioner();
        for (final Vm vm : vmList) {
            ramProvisioner.deallocateResourceForVm(vm);
            bwProvisioner.deallocateResourceForVm(vm);
            peProvisioner.deallocateResourceForVm(vm);
            vm.setCreated(false);
            disk.getStorage().deallocateResource(vm.getStorage());
        }

        vmList.clear();
    }

    @Override
    public GpuHost addOnStartupListener(final EventListener<HostEventInfo> listener) {
        if(EventListener.NULL.equals(listener)){
            return this;
        }

        onStartupListeners.add(requireNonNull(listener));
        return this;
    }

    @Override
    public boolean removeOnStartupListener(final EventListener<HostEventInfo> listener) {
        return onStartupListeners.remove(listener);
    }

    @Override
    public GpuHost addOnShutdownListener(final EventListener<HostEventInfo> listener) {
        if(EventListener.NULL.equals(listener)){
            return this;
        }

        onShutdownListeners.add(requireNonNull(listener));
        return this;
    }

    @Override
    public boolean removeOnShutdownListener(final EventListener<HostEventInfo> listener) {
        return onShutdownListeners.remove(listener);
    }

    @Override
    public long getNumberOfPes() {
        return peList.size();
    }

    protected MipsShare getAllocatedMipsForVm(final Vm vm) {
        return vmScheduler.getAllocatedMips(vm);
    }

    @Override
    public double getMips() {
        return peList.stream().mapToDouble(Pe::getCapacity).findFirst().orElse(0);
    }

    @Override
    public double getTotalMipsCapacity() {
        return peList.stream()
                     .filter(Pe::isWorking)
                     .mapToDouble(Pe::getCapacity)
                     .sum();
    }

    @Override
    public double getTotalAvailableMips() {
        return vmScheduler.getTotalAvailableMips();
    }

    @Override
    public double getTotalAllocatedMips() {
        return getTotalMipsCapacity() - getTotalAvailableMips();
    }

    @Override
    public double getTotalAllocatedMipsForVm(final Vm vm) {
        return vmScheduler.getTotalAllocatedMipsForVm(vm);
    }

    @Override
    public Resource getBw() {
        return bwProvisioner.getPmResource();
    }

    @Override
    public Resource getRam() {
        return ramProvisioner.getPmResource();
    }

    @Override
    public FileStorage getStorage() {
        return disk;
    }

    @Override
    public long getId() {
        return id;
    }

    /*@Override
    public final void setId(long id) {
        this.id = id;
    }*/

    @Override
    public ResourceProvisioner getRamProvisioner() {
        return ramProvisioner;
    }

    /*@Override
    public final GpuHost setRamProvisioner(final ResourceProvisioner ramProvisioner) {
        checkSimulationIsRunningAndAttemptedToChangeHost("RAM");
        this.ramProvisioner = requireNonNull(ramProvisioner);
        this.ramProvisioner.setResources(ram, vm -> ((GpuVmSimple)vm).getRam());
        return this;
    }*/

    private void checkSimulationIsRunningAndAttemptedToChangeHost(final String resourceName) {
        if(simulation.isRunning()){
            final var msg = "It is not allowed to change a Host's %s after the simulation started.";
            throw new IllegalStateException(String.format(msg, resourceName));
        }
    }

    @Override
    public ResourceProvisioner getBwProvisioner() {
        return bwProvisioner;
    }

    /*@Override
    public final GpuHost setBwProvisioner(final ResourceProvisioner bwProvisioner) {
        checkSimulationIsRunningAndAttemptedToChangeHost("BW");
        this.bwProvisioner = requireNonNull(bwProvisioner);
        this.bwProvisioner.setResources(bw, vm -> ((GpuVmSimple)vm).getBw());
        return this;
    }*/

    @Override
    public VmScheduler getVmScheduler() {
        return vmScheduler;
    }

    /*@Override
    public final GpuHost setVmScheduler(final VmScheduler vmScheduler) {
        this.vmScheduler = requireNonNull(vmScheduler);
        vmScheduler.setHost(this);
        return this;
    }*/

    @Override
    public double getStartTime() {
        return startTime;
    }

    @Override
    public double getFirstStartTime(){
        return firstStartTime;
    }

    @Override
    public GpuHost setStartTime(final double startTime) {
        if(startTime < 0){
            throw new IllegalArgumentException("Host start time cannot be negative");
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
            throw new IllegalArgumentException("Host shutdown time cannot be negative");
        }

        this.shutdownTime = Math.floor(shutdownTime);
        this.totalUpTime += getUpTime();
    }

    @Override
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
    }

    @Override
    public double getIdleShutdownDeadline() {
        return idleShutdownDeadline;
    }

    @Override
    public GpuHost setIdleShutdownDeadline(final double deadline) {
        this.idleShutdownDeadline = deadline;
        return this;
    }

    @Override
    public List<Pe> getPeList() {
        return peList;
    }
    

    /**
     * Sets the PE list.
     *
     * @param peList the new pe list
     */
    private void setPeList(final List<Pe> peList) {
        if(requireNonNull(peList).isEmpty()){
            throw new IllegalArgumentException("The PE list for a Host cannot be empty");
        }

        checkSimulationIsRunningAndAttemptedToChangeHost("List of PE");
        this.peList = peList;

        long peId = Math.max(peList.get(peList.size()-1).getId(), -1);
        for(final Pe pe: peList){
            if(pe.getId() < 0) {
                pe.setId(++peId);
            }
            pe.setStatus(Pe.Status.FREE);
        }

        failedPesNumber = 0;
        busyPesNumber = 0;
        freePesNumber = peList.size();
        workingPesNumber = freePesNumber;
    }

    @Override
    public <T extends Vm> List<T> getVmList() {
        return (List<T>) vmList;
    }

    @Override
    public <T extends Vm> List<T> getVmCreatedList() {
        return (List<T>) Collections.unmodifiableList(vmCreatedList);
    }

    protected void addVmToList(final Vm vm){
        vmList.add(requireNonNull(vm));
    }

    protected void addVmToCreatedList(final Vm vm){
        vmCreatedList.add(requireNonNull(vm));
    }

    @Override
    public boolean isFailed() {
        return failed;
    }

    /*@Override
    public final boolean setFailed(final boolean failed) {
        this.failed = failed;
        final Pe.Status newStatus = failed ? Pe.Status.FAILED : Pe.Status.FREE;
        setPeStatus(peList, newStatus);

        /*Just changes the active state when the Host is set to active.
        * In other situations, the active status must remain as it was.
        * For example, if the host was inactive and now it's set to failed,
        * it must remain inactive.
        if(failed && this.active){
            this.active = false;
        }

        return true;
    }

    public final void setPeStatus(final List<Pe> peList, final Pe.Status newStatus){
        /*For performance reasons, stores the number of free and failed PEs
        instead of iterating over the PE list every time to find out.
        for (final Pe pe : peList) {
            updatePeStatus(pe, newStatus);
        }
    }*/

    private void updatePeStatus(final Pe pe, final Pe.Status newStatus) {
        if(pe.getStatus() != newStatus) {
            updatePeStatusCount(pe.getStatus(), false);
            updatePeStatusCount(newStatus, true);
            pe.setStatus(newStatus);
        }
    }

    private void updatePeStatusCount(final Pe.Status status, final boolean isIncrement) {
        final int inc = isIncrement ? 1 : -1;
        switch (status) {
            case FAILED -> incFailedPesNumber(inc);
            case FREE   -> incFreePesNumber(inc);
            case BUSY   -> incBusyPesNumber(inc);
        }
    }

    protected void incFailedPesNumber(final int inc) {
        this.failedPesNumber += inc;
        workingPesNumber -= inc;
    }

    protected void incFreePesNumber(final int inc) {
        this.freePesNumber += inc;
    }

    protected void incBusyPesNumber(final int inc) {
        this.busyPesNumber += inc;
    }

    @Override
    public <T extends Vm> Set<T> getVmsMigratingIn() {
        return (Set<T>)vmsMigratingIn;
    }

    @Override
    public boolean hasMigratingVms(){
        return !(vmsMigratingIn.isEmpty() && vmsMigratingOut.isEmpty());
    }

    /*@Override
    public boolean addMigratingInVm(final Vm vm) {
        /* TODO: Instead of keeping a list of VMs which are migrating into a Host,
        *  which requires searching in such a list every time a VM is requested to be migrated
        *  to that Host (to check if it isn't migrating to that same host already),
        *  we can add a migratingHost attribute to Vm, so that the worst time complexity
        *  will change from O(N) to a constant time O(1). 
        if (vmsMigratingIn.contains(vm)) {
            return false;
        }

        if(!allocateResourcesForVm(vm, true).fully()){
            return false;
        }

        ((GpuVmSimple)vm).updateMigrationStartListeners(this);

        updateProcessing(simulation.clock());
        vm.getHost().updateProcessing(simulation.clock());

        return true;
    }*/

    @Override
    public void removeMigratingInVm(final Vm vm) {
        vmsMigratingIn.remove(vm);
        vmList.remove(vm);
        vm.setInMigration(false);
    }

    @Override
    public Set<Vm> getVmsMigratingOut() {
        return Collections.unmodifiableSet(vmsMigratingOut);
    }

    @Override
    public boolean addVmMigratingOut(final Vm vm) {
        return this.vmsMigratingOut.add(vm);
    }

    @Override
    public boolean removeVmMigratingOut(final Vm vm) {
        return this.vmsMigratingOut.remove(vm);
    }

    @Override
    public GpuDatacenter getDatacenter() {
        return datacenter;
    }

    /*@Override
    public final void setDatacenter(final Datacenter datacenter) {
        if(!GpuDatacenter.NULL.equals(this.datacenter)) {
            checkSimulationIsRunningAndAttemptedToChangeHost("Datacenter");
        }

        this.datacenter = (GpuDatacenter)datacenter;
    }*/

    @Override
    public String toString() {
        final String dc =
                datacenter == null || GpuDatacenter.NULL.equals(datacenter) ? "" :
                String.format("/DC %d", datacenter.getId());
        return String.format("Host %d%s", getId(), dc);
    }

    @Override
    public boolean removeOnUpdateProcessingListener(final EventListener<HostUpdatesVmsProcessingEventInfo> listener) {
        return onUpdateProcessingListeners.remove(listener);
    }

    @Override
    public GpuHost addOnUpdateProcessingListener(
    		final EventListener<HostUpdatesVmsProcessingEventInfo> listener) {
        if(EventListener.NULL.equals(listener)){
            return this;
        }

        this.onUpdateProcessingListeners.add(requireNonNull(listener));
        return this;
    }

    @Override
    public long getAvailableStorage() {
        return disk.getAvailableResource();
    }

    @Override
    public int getFreePesNumber() {
        return freePesNumber;
    }

    @Override
    public int getWorkingPesNumber() {
        return workingPesNumber;
    }

    @Override
    public int getBusyPesNumber() {
        return busyPesNumber;
    }

    @Override
    public double getBusyPesPercent() {
        return getBusyPesNumber() / (double)getNumberOfPes();
    }

    @Override
    public double getBusyPesPercent(final boolean hundredScale) {
        final double scale = hundredScale ? 100 : 1;
        return getBusyPesPercent() * scale;
    }

    @Override
    public int getFailedPesNumber() {
        return failedPesNumber;
    }

    @Override
    public Simulation getSimulation() {
        return this.simulation;
    }

    @Override
    public double getLastBusyTime() {
        return lastBusyTime;
    }

    /*@Override
    public final GpuHost setSimulation(final Simulation simulation) {
        this.simulation = simulation;
        return this;
    }*/

    @Override
    public int compareTo(final Host other) {
        if(this.equals(requireNonNull(other))) {
            return 0;
        }

        return Long.compare(this.id, other.getId());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final GpuHostSimple that = (GpuHostSimple) obj;
        return this.getId() == that.getId() && this.simulation.equals(that.simulation);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + simulation.hashCode();
        return result;
    }

    @Override
    public List<ResourceManageable> getResources() {
        if(simulation.isRunning() && resources.isEmpty()){
            resources = Arrays.asList(ram, bw);
        }

        return Collections.unmodifiableList(resources);
    }

    @Override
    public ResourceProvisioner getProvisioner(final Class<? extends ResourceManageable> resourceClass) {
        if(simulation.isRunning() && provisioners.isEmpty()){
            provisioners = Arrays.asList(ramProvisioner, bwProvisioner);
        }

        return provisioners
            .stream()
            .filter(provisioner -> provisioner.getPmResource().isSubClassOf(resourceClass))
            .findFirst()
            .orElse(ResourceProvisioner.NULL);
    }

    @Override
    public List<Pe> getWorkingPeList() {
        return getFilteredPeList(Pe::isWorking);
    }

    @Override
    public List<Pe> getBusyPeList() {
        return getFilteredPeList(Pe::isBusy);
    }

    @Override
    public List<Pe> getFreePeList() {
        return getFilteredPeList(Pe::isFree);
    }

    private List<Pe> getFilteredPeList(final Predicate<Pe> status) {
        return peList.stream().filter(status).collect(toList());
    }

    @Override
    public double getCpuPercentUtilization() {
        return computeCpuUtilizationPercent(getCpuMipsUtilization());
    }

    @Override
    public double getCpuPercentRequested() {
        return computeCpuUtilizationPercent(getCpuMipsRequested());
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
    public double getCpuMipsUtilization() {
        return vmList.stream().mapToDouble(Vm::getTotalCpuMipsUtilization).sum();
    }

    private double getCpuMipsRequested() {
        return vmList.stream().mapToDouble(Vm::getTotalCpuMipsRequested).sum();
    }

    @Override
    public long getRamUtilization() {
        return ramProvisioner.getTotalAllocatedResource();
    }

    @Override
    public long getBwUtilization() {
        return bwProvisioner.getTotalAllocatedResource();
    }

    @Override
    public HostResourceStats getCpuUtilizationStats() {
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

    @Override
    public PowerModelHost getPowerModel() {
        return powerModel;
    }

    /*@Override
    public final void setPowerModel(final PowerModelHost powerModel) {
        requireNonNull(powerModel,
            "powerModel cannot be null. You could provide a " +
            PowerModelHost.class.getSimpleName() + ".NULL instead.");

        if(powerModel.getHost() != null && powerModel.getHost() != GpuHost.NULL && !this.equals(powerModel.getHost())){
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
    public List<Vm> getFinishedVms() {
        return getVmList().stream()
            .filter(vm -> !vm.isInMigration())
            .filter(vm -> vm.getTotalCpuMipsRequested() == 0)
            .collect(toList());
    }

    private double addVmResourceUseToHistoryIfNotMigratingIn(final Vm vm, final double currentTime) {
        double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);
        if (getVmsMigratingIn().contains(vm)) {
            LOGGER.info("{}: {}: {} is migrating in", getSimulation().clockStr(), this, vm);
            return totalAllocatedMips;
        }

        final double totalRequestedMips = vm.getTotalCpuMipsRequested();
        if (totalAllocatedMips + 0.1 < totalRequestedMips) {
            final String reason = getVmsMigratingOut().contains(vm) ? "migration overhead" : "capacity unavailability";
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

        for (final Vm vm : getVmList()) {
            final double totalRequestedMips = vm.getTotalCpuMipsRequested();
            addVmResourceUseToHistoryIfNotMigratingIn(vm, currentTime);
            hostTotalRequestedMips += totalRequestedMips;
        }

        addStateHistoryEntry(currentTime, getCpuMipsUtilization(), hostTotalRequestedMips, active);
    }

    private void addStateHistoryEntry(
        final double time,
        final double allocatedMips,
        final double requestedMips,
        final boolean isActive)
    {
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
    public List<HostStateHistoryEntry> getStateHistory() {
        return Collections.unmodifiableList(stateHistory);
    }

    @Override
    public List<Vm> getMigratableVms() {
        return vmList.stream().filter(vm -> !vm.isInMigration()).collect(toList());
    }

    @Override
    public boolean isLazySuitabilityEvaluation() {
        return lazySuitabilityEvaluation;
    }

    @Override
    public GpuHost setLazySuitabilityEvaluation(final boolean lazySuitabilityEvaluation) {
        this.lazySuitabilityEvaluation = lazySuitabilityEvaluation;
        return this;
    }

    public boolean isActivateOnDatacenterStartup() {
        return activateOnDatacenterStartup;
    }
}
