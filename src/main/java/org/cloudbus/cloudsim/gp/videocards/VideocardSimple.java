package org.cloudbus.cloudsim.gp.videocards;

import org.cloudbus.cloudsim.gp.provisioners.VideocardBwProvisioner;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicySimple;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicy;
//import org.gpucloudsimplus.listeners.VideocardVGpuMigrationEventInfo;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.resources.GpuSimple;
//import org.cloudbus.cloudsim.gp.vms.GpuVm;
//import org.cloudbus.cloudsim.hosts.Host;
import org.gpucloudsimplus.listeners.GpuEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.gp.resources.Gpu;
//import org.cloudbus.cloudsim.gp.resources.GpuSimple;
//import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenterSimple;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;
import org.cloudbus.cloudsim.core.Simulation;
//import org.cloudbus.cloudsim.gp.core.GpuCloudsimTags;
import static org.cloudbus.cloudsim.util.BytesConversion.bitesToBytes;

import java.util.*;
import java.util.stream.Stream;
//import java.util.function.Predicate;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class VideocardSimple implements Videocard {
	
	//private long id;
	//private String type;
	private GpuHost host;
	private Simulation simulation;
	
	private List<? extends Gpu> gpuList;
	
	private VGpuAllocationPolicy vgpuAllocationPolicy;
	private VideocardBwProvisioner pcieBwProvisioner;
	
	private double schedulingInterval;
	
	private final List<EventListener<GpuEventInfo>> onGpuAvailableListeners;
    //private final List<EventListener<VideocardVGpuMigrationEventInfo>> onVGpuMigrationFinishListeners;

    private Map<VGpu, Gpu> lastMigrationMap;

    private double gpuSearchRetryDelay;
    
    private long activeGpusNumber;
    
    private double bandwidthPercentForMigration;
    
    private boolean migrationsEnabled = false;
    
    private double lastUnderOrOverloadedDetection = -Double.MAX_VALUE;

    private double lastProcessTime;
    
    public VideocardSimple (final List<? extends Gpu> gpuList) {
    	this(gpuList, new VGpuAllocationPolicySimple());
    }
    
	public VideocardSimple (final List<? extends Gpu> gpuList,
	        final VGpuAllocationPolicy vgpuAllocationPolicy) {
		
        this.setSimulation(Simulation.NULL);
		this.setGpuList(gpuList);
        this.setLastProcessTime(0.0);
        this.setSchedulingInterval(0);
        //setPowerModel(new PowerModelDatacenterSimple(this));
        
        this.onGpuAvailableListeners = new ArrayList<>();
        //this.onVGpuMigrationFinishListeners = new ArrayList<>();
        //this.bandwidthPercentForMigration = DEF_BW_PERCENT_FOR_MIGRATION;
        //this.migrationsEnabled = true;
        this.gpuSearchRetryDelay = -1;

        //this.lastMigrationMap = Collections.emptyMap();
        this.migrationsEnabled = false;
        this.setVGpuAllocationPolicy(vgpuAllocationPolicy);
	}
	
	@Override
	public boolean processVGpuCreate (final VGpu vgpu) {
        final boolean gpuAllocatedForVGpu = vgpuAllocationPolicy.allocateGpuForVGpu(vgpu).fully();
        if (gpuAllocatedForVGpu)
            vgpu.updateGpuTaskProcessing(vgpu.getGpu().getVGpuScheduler().getAllocatedMips(vgpu));
        return gpuAllocatedForVGpu;
    }

	@Override
    public VGpuAllocationPolicy getVGpuAllocationPolicy () {
        return vgpuAllocationPolicy;
    }
	
	@Override
    public String processVGpuDestroy (final VGpu vgpu) {
		getVGpuAllocationPolicy().deallocateGpuForVGpu(vgpu);
		return generateNotFinishedGpuTasksWarning (vgpu);
	}
	
	private String generateNotFinishedGpuTasksWarning (final VGpu vgpu) {
		final int gpuTasksNoFinished = vgpu.getGpuTaskScheduler().getGpuTaskList().size();

		if(gpuTasksNoFinished == 0) {
            return "";
        }

        return String.format("It had a total of %d gpuTask (running + waiting)", gpuTasksNoFinished);
    }
	
	@Override
    public <T extends Gpu> List<T> getGpuList () {
        return (List<T>)Collections.unmodifiableList(gpuList);
    }
	
	@Override
	public void updateGpusProcessing () {
        for (final Gpu gpu : getGpuList()) {
            final double delay = gpu.updateProcessing(clock());
        }
    }
	
	@Override
	public void processGpuAdditionRequest () {
		for (final Gpu gpu : getGpuList()) {
			notifyOnGpuAvailableListeners(gpu);
		}
	}
	
	private <T extends Gpu> void notifyOnGpuAvailableListeners (final T gpu) {
        onGpuAvailableListeners.forEach(listener -> listener.update(GpuEventInfo.of(
        		listener, gpu, clock())));
    }
	
	@Override
	public void gpusProcessActivation (final boolean activate) {
		for (final Gpu gpu : getGpuList()) {
			gpu.processActivation(activate);
		}
	}
	
	@Override
	public void gpuProcessActivation (final Gpu gpu, final boolean activate) {
		gpu.processActivation(activate);
	}
	
	/*@Override 
	public long getId () {
		return id;
	}
	
	@Override 
	public void setId (long id) {
		this.id = id;
	}
	
	@Override 
	public String getType () {
		return type;
	}
	
	@Override 
	public void setType (String type) {
		this.type = type;
	}*/
	
	/*@Override
    public void videocardProcess () {
		// TODO
	}*/

	@Override
    public String toString () {
        return String.format("Videocard in host %d", host.getId());
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        final VideocardSimple that = (VideocardSimple) object;

        return this.host.getId() == that.host.getId() && 
        		this.getSimulation().equals(that.getSimulation());
    }
    
    

	private void setGpuList (final List<? extends Gpu> gpuList) {
        this.gpuList = requireNonNull(gpuList);
        long lastGpuId = this.gpuList.isEmpty() ? -1 : 
        	this.gpuList.get(this.gpuList.size()-1).getId();
        for (final Gpu gpu : this.gpuList) {
        	lastGpuId = Math.max(lastGpuId, -1);
            if(gpu.getId() < 0) {
                gpu.setId(++lastGpuId);
            }
            gpu.setSimulation(getSimulation()).setVideocard(this);
            gpu.setActive(((GpuSimple)gpu).isActivateOnVideocardStartup());
        }
    }
	

    @Override
    public Stream<? extends Gpu> getActiveGpuStream() {
        return gpuList.stream().filter(Gpu::isActive);
    }

	@Override
	public Simulation getSimulation () {
		return simulation;
	}
	
	private double clock() {
        return getSimulation().clock();
    }

	@Override
    public Gpu getGpu (final int index) {
        if (index >= 0 && index < getGpuList().size()) {
            return getGpuList().get(index);
        }

        return Gpu.NULL;
    }
	
	//Gets the total number of existing Gpus in this Videocard,
    //which indicates the Videocard's size.
	@Override
    public long size () {
        return gpuList.size();
    }
	
    @Override
    public long getActiveGpusNumber(){
        return activeGpusNumber;
    }


    @Override
    public Gpu getGpuById (final long id) {
        return gpuList.stream().filter(gpu -> gpu.getId() == id).findFirst().map(
        		gpu -> (Gpu)gpu).orElse(Gpu.NULL);
    }

    @Override
    public <T extends Gpu> Videocard addGpuList (final List<T> gpuList) {
        requireNonNull(gpuList);
        gpuList.forEach(this::addGpu);
        return this;
    }

	@Override
    public <T extends Gpu> Videocard addGpu (final T gpu) {
        if(vgpuAllocationPolicy == null || vgpuAllocationPolicy == VGpuAllocationPolicy.NULL) {
            throw new IllegalStateException("A VGpuAllocationPolicy must be set before adding a new "
            		+ "Gpu to the Videocard.");
        }

        long nextId = gpuList.isEmpty() ? -1 : gpuList.get(gpuList.size()-1).getId();
        nextId = Math.max(nextId, -1);
        if(gpu.getId() < 0) 
            gpu.setId(++nextId);
        
        gpu.setActive(((GpuSimple)gpu).isActivateOnVideocardStartup());
        ((List<T>)gpuList).add(gpu);
        return this;
    }

    @Override
    public <T extends Gpu> Videocard removeGpu (final T gpu) {
        gpuList.remove(gpu);
        return this;
    }

	
	
	@Override
    public final Videocard setVGpuAllocationPolicy ( final VGpuAllocationPolicy vgpuAllocationPolicy) {
        requireNonNull(vgpuAllocationPolicy);
        if(vgpuAllocationPolicy.getVideocard() != null && 
        		vgpuAllocationPolicy.getVideocard() != Videocard.NULL && 
        		!this.equals(vgpuAllocationPolicy.getVideocard())){
            throw new IllegalStateException("The given vgpuAllocationPolicy is already used by another"
            		+ " Videocard.");
        }

        vgpuAllocationPolicy.setVideocard(this);
        this.vgpuAllocationPolicy = vgpuAllocationPolicy;
        return this;
    }

    @Override
    public double getSchedulingInterval () {
        return schedulingInterval;
    }

    @Override
    public final Videocard setSchedulingInterval (final double schedulingInterval) {
        this.schedulingInterval = Math.max(schedulingInterval, 0);
        return this;
    }
    
    @Override
    public Videocard addOnGpuAvailableListener (final EventListener<GpuEventInfo> listener) {
        onGpuAvailableListeners.add(requireNonNull(listener));
        return this;
    }

    @Override
    public boolean isMigrationsEnabled() {
        return migrationsEnabled && vgpuAllocationPolicy.isVGpuMigrationSupported();
    }
    
	@Override
    public double getGpuSearchRetryDelay () {
        return gpuSearchRetryDelay;
    }
	
	@Override
    public Videocard setGpuSearchRetryDelay (final double delay) {
        if(delay == 0){
            throw new IllegalArgumentException("gpuSearchRetryDelay cannot be 0. Set a positive value "
            		+ "to define an actual delay or a negative value to indicate a new gpu search "
            		+ "must be tried as soon as possible.");
        }

        this.gpuSearchRetryDelay = delay;
        return this;
    }
	
	public <T extends VGpu> List<T> getVGpuList () {
        return (List<T>) Collections.unmodifiableList(
                getGpuList()
                    .stream()
                    .map(Gpu::getVGpuList)
                    .flatMap(List::stream)
                    .collect(toList()));
    }

    public void updateActiveGpusNumber (final Gpu gpu){
        activeGpusNumber += gpu.isActive() ? 1 : -1;
    }

    
    
    protected double getLastProcessTime () {
        return lastProcessTime;
    }

    protected final void setLastProcessTime (final double lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }
    

	private boolean isTimeToSearchForSuitableGpus () {
        final double elapsedSecs = clock() - lastUnderOrOverloadedDetection;
        return isMigrationsEnabled() && elapsedSecs >= gpuSearchRetryDelay;
    }

    private boolean areThereUnderOrOverloadedGpusAndMigrationIsSupported (){
        /*if(vgpuAllocationPolicy instanceof VGpuAllocationPolicyMigration migrationPolicy){
            return migrationPolicy.areGpusUnderOrOverloaded();
        }*/
        return false;
    }
    
	//sarnakh :BROKER NAME
	/*private void notifyBrokerAboutAlreadyFinishedGpuTask(final GpuTask gpuTask, final boolean ack) {
        LOGGER.warn(
            "{}: {} owned by {} is already completed/finished. It won't be executed again.",
            getName(), gpuTask, gpuTask.getBroker());

        sendCloudletSubmitAckToBroker(gpuTask, ack);

        sendNow(gpuTask.getBroker(), GpuCloudsimTags.GPUTASK_RETURN, gpuTask);
    }*/
	
	//sarnakh :BROKER NAME
	/*private void sendCloudletSubmitAckToBroker(final GpuTask gpuTask, final boolean ack) {
        if(!ack){
            return;
        }

        sendNow(gpuTask.getBroker(), GpuCloudsimTags.GPUTASK_SUBMIT_ACK, gpuTask);
    }*/
	
	
	
	/*protected double updateGpuTaskProcessing() {
        if (!isTimeToUpdateGpuTasksProcessing()){
            return Double.MAX_VALUE;
        }

        double nextSimulationDelay = updateGpusProcessing();

        if (nextSimulationDelay != Double.MAX_VALUE) {
        	//simplify with delete Interval
            nextSimulationDelay = getGpuTaskProcessingUpdateInterval (nextSimulationDelay);
            schedule(nextSimulationDelay, GpuCloudsimTags.VGPU_UPDATE_GPUTASK_PROCESSING);
        }
        setLastProcessTime(clock());

        checkIfVGpuMigrationsAreNeeded();
        return nextSimulationDelay;
    }*/
	
	/*private boolean isTimeToUpdateGpuTasksProcessing() {
        return clock() < 0.111 ||
               clock() >= lastProcessTime + getSimulation().getMinTimeBetweenEvents();
    }*/
	
	private double timeToMigrateVGpu (final VGpu vgpu, final Gpu targetGpu) {
        return vgpu.getGddram().getCapacity() / bitesToBytes(targetGpu.getBw().getCapacity() * 
        		getBandwidthPercentForMigration());
    }

	private void checkIfVGpuMigrationsAreNeeded () {
        if (!isTimeToSearchForSuitableGpus()) {
            return;
        }

        /*lastMigrationMap = vgpuAllocationPolicy.getOptimizedAllocationMap(getVGpuList());
        for (final Map.Entry<VGpu, Gpu> entry : lastMigrationMap.entrySet()) {
            requestVGpuMigration(entry.getKey(), entry.getValue());
        }

        if(areThereUnderOrOverloadedGpusAndMigrationIsSupported()){
            lastUnderOrOverloadedDetection = clock();
        }*/
    }
    
	/*@Override
    public void requestVGpuMigration(final VGpu sourceVGpu) {
        requestVGpuMigration(sourceVGpu, Gpu.NULL);
    }

    @Override
    public void requestVGpuMigration(final VGpu sourceVGpu, Gpu targetGpu) {
        if(Gpu.NULL.equals(targetGpu)){
            targetGpu = vgpuAllocationPolicy.findGpuForVGpu(sourceVGpu).orElse(Gpu.NULL);
        }

        if(Gpu.NULL.equals(targetGpu)) {
            LOGGER.warn("{}: {}: No suitable Gpu found for {} in {}", 
            		sourceVGpu.getSimulation().clockStr(), getClass().getSimpleName(), 
            		sourceVGpu, this);
            return;
        }

        final Gpu sourceGpu = sourceVGpu.getGpu ();
        final double delay = timeToMigrateVGpu(sourceVGpu, targetGpu);
        final String msg1 =
        		Gpu.NULL.equals(sourceGpu) ?
                String.format("%s to %s", sourceVGpu, targetGpu) :
                String.format("%s from %s to %s", sourceVGpu, sourceGpu, targetGpu);

        final String currentTime = getSimulation().clockStr();
        final String fmt = "It's expected to finish in %.2f seconds, considering the %.0f%% of "
        		+ "bandwidth allowed for migration and the VGpu RAM size.";
        final String msg2 = String.format(fmt, delay, getBandwidthPercentForMigration()*100);
        LOGGER.info("{}: {}: Migration of {} is started. {}", currentTime, getName(), msg1, msg2);

        if(targetGpu.addMigratingInVGpu(sourceVGpu)) {
            sourceGpu.addVGpuMigratingOut(sourceVGpu);
            send(this, delay, GpuCloudsimTags.VGPU_MIGRATE, new TreeMap.SimpleEntry<>(sourceVGpu, 
            		targetGpu));
        }
    }*/
    
	@Override
    public double getBandwidthPercentForMigration () {
        return bandwidthPercentForMigration;
    }

    /*@Override
    public void setBandwidthPercentForMigration (final double bandwidthPercentForMigration) {
        if(bandwidthPercentForMigration <= 0){
            throw new IllegalArgumentException("The bandwidth migration percentage must be greater "
            		+ "than 0.");
        }

        if(bandwidthPercentForMigration > 1){
            throw new IllegalArgumentException("The bandwidth migration percentage must be lower or "
            		+ "equal to 1.");
        }

        this.bandwidthPercentForMigration = bandwidthPercentForMigration;
    }*/
    
	

    /*@Override
    public Videocard addOnVGpuMigrationFinishListener (
    		final EventListener<VideocardVGpuMigrationEventInfo> listener) {
        onVGpuMigrationFinishListeners.add(requireNonNull(listener));
        return this;
    }*/

	/*@Override
    public final Videocard enableMigrations () {
        if(!vgpuAllocationPolicy.isVGpuMigrationSupported()){
            LOGGER.warn(
                "{}: {}: It was requested to enable VGpu migrations but the {} doesn't support that.",
                getSimulation().clockStr(), getName(),
                vgpuAllocationPolicy.getClass().getSimpleName());
            return this;
        }

        this.migrationsEnabled = true;
        return this;
    }

    @Override
    public final Videocard disableMigrations () {
        this.migrationsEnabled = false;
        return this;
    }*/
	
	@Override 
	public VideocardBwProvisioner getPcieBwProvisioner () {
		return pcieBwProvisioner;
	}
	
	@Override
	public GpuHost getHost () {
		return host;
	}
	
	@Override
	public Videocard setHost (GpuHost host) {
		this.host = host;
    	if (!host.hasVideocard())
    		host.setVideocard(this);
    	return this;
	}
	
	@Override public Videocard setPcieBwProvisioner (VideocardBwProvisioner pcieBwProvisioner) {
		this.pcieBwProvisioner = pcieBwProvisioner;
		return this;
	}

	@Override
	public void setBandwidthPercentForMigration (double bandwidthPercentForMigration) {
		this.bandwidthPercentForMigration = bandwidthPercentForMigration;
	}
	
	@Override
	public boolean hasGpuHost () {
		return getHost() != null;
	}
	
	@Override
	public Videocard setSimulation (Simulation simulation) {
		this.simulation = simulation;
		return this;
	}

}

