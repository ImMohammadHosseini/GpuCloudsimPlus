package org.cloudbus.cloudsim.gp.datacenters;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.CloudSimTag;
import org.cloudbus.cloudsim.network.IcmpPacket;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.resources.SanStorage;
import org.cloudbus.cloudsim.hosts.HostSuitability;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.core.events.PredicateType;
import org.cloudbus.cloudsim.core.CustomerEntityAbstract;
import org.cloudbus.cloudsim.resources.DatacenterStorage;
import org.cloudbus.cloudsim.power.models.PowerModelDatacenter;
import org.cloudbus.cloudsim.util.InvalidEventDataTypeException;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.power.models.PowerModelDatacenterSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristicsSimple;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;
import org.cloudbus.cloudsim.gp.vms.GpuVmSimple;
import org.cloudbus.cloudsim.gp.hosts.GpuHostSimple;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudlet;
import org.cloudbus.cloudsim.gp.resources.GpuSuitability;
import org.cloudbus.cloudsim.gp.vgpu.VGpuSimple;
import org.cloudbus.cloudsim.gp.allocationpolicies.GpuVmAllocationPolicy;
import org.cloudbus.cloudsim.gp.allocationpolicies.GpuVmAllocationPolicySimple;

import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.HostEventInfo;
import org.cloudsimplus.autoscaling.VerticalVmScaling;
import org.cloudsimplus.faultinjection.HostFaultInjection;
import org.cloudsimplus.listeners.DatacenterVmMigrationEventInfo;

import java.util.*;
import java.util.stream.Stream;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.cloudbus.cloudsim.util.BytesConversion.bitesToBytes;

public class GpuDatacenterSimple extends CloudSimEntity implements GpuDatacenter {

    private double lastUnderOrOverloadedDetection = -Double.MAX_VALUE;
    private double bandwidthPercentForMigration;

    private boolean migrationsEnabled;

    private List<? extends GpuHost> gpuhostList;
    private final DatacenterCharacteristics characteristics;
    private GpuVmAllocationPolicy gpuVmAllocationPolicy;

    private double lastProcessTime;
    private double schedulingInterval;
    
	private DatacenterStorage datacenterStorage;

    private final List<EventListener<HostEventInfo>> onGpuHostAvailableListeners;
    private final List<EventListener<DatacenterVmMigrationEventInfo>> onGpuVmMigrationFinishListeners;

    private double timeZone;
    private Map<GpuVm, GpuHost> lastMigrationMap;

    private double gpuhostSearchRetryDelay;

    private PowerModelDatacenter powerModel = PowerModelDatacenter.NULL;
    private long activeHostsNumber;

    public GpuDatacenterSimple (final Simulation simulation, 
    		final List<? extends GpuHost> hostList) {
        this(simulation, hostList, new GpuVmAllocationPolicySimple(), new DatacenterStorage());
    }

    public GpuDatacenterSimple (final Simulation simulation, final List<? extends GpuHost> hostList,
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy) {
    	this(simulation, hostList, gpuVmAllocationPolicy, new DatacenterStorage());
    }

    public GpuDatacenterSimple ( final Simulation simulation, 
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy) {
        this(simulation, new ArrayList<>(), gpuVmAllocationPolicy, new DatacenterStorage());
    }

    public GpuDatacenterSimple (final Simulation simulation, 
    		final List<? extends GpuHost> hostList,
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy,final List<SanStorage> storageList) {
        this(simulation, hostList, gpuVmAllocationPolicy, new DatacenterStorage(storageList));
    }
    
    public GpuDatacenterSimple (final Simulation simulation, final List<? extends GpuHost> hostList,
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy, final DatacenterStorage storage) {
        super(simulation);
        setGpuHostList(hostList);
        setLastProcessTime(0.0);
        setSchedulingInterval(0);
        setDatacenterStorage(storage);
        setPowerModel(new PowerModelDatacenterSimple(this));

        this.onGpuHostAvailableListeners = new ArrayList<>();
        this.onGpuVmMigrationFinishListeners = new ArrayList<>();
        this.characteristics = new DatacenterCharacteristicsSimple(this);
        this.bandwidthPercentForMigration = DEF_BW_PERCENT_FOR_MIGRATION;
        this.migrationsEnabled = true;
        this.gpuhostSearchRetryDelay = -1;

        this.lastMigrationMap = Collections.emptyMap();

        setGpuVmAllocationPolicy(gpuVmAllocationPolicy);
    }
    
    public final GpuDatacenter setGpuVmAllocationPolicy (
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy) {
        requireNonNull(gpuVmAllocationPolicy);
        if(gpuVmAllocationPolicy.getDatacenter() != null && 
        		gpuVmAllocationPolicy.getDatacenter() != Datacenter.NULL && 
        		!this.equals(gpuVmAllocationPolicy.getDatacenter())){
            throw new IllegalStateException("The given GpuVmAllocationPolicy is already used by "
            		+ "another Datacenter.");
        }

        gpuVmAllocationPolicy.setDatacenter(this);
        this.gpuVmAllocationPolicy = gpuVmAllocationPolicy;
        return this;
    }
    
    private void setGpuHostList (final List<? extends GpuHost> hostList) {
        this.gpuhostList = requireNonNull(hostList);
        setupGpuHosts();
    }
    
    private void setupGpuHosts () {
        long lastGpuHostId = getLastGpuHostId();
        for (final GpuHost host : gpuhostList)
            lastGpuHostId = setupGpuHost (host, lastGpuHostId);
    }
    
    protected double getLastProcessTime() {
        return lastProcessTime;
    }
    
    private long getLastGpuHostId () {
        return gpuhostList.isEmpty() ? -1 : gpuhostList.get(gpuhostList.size()-1).getId();
    }
    
    protected long setupGpuHost (final GpuHost host, long nextId) {
        nextId = Math.max(nextId, -1);
        if(host.getId() < 0) {
            host.setId(++nextId);
        }

        host.setSimulation(getSimulation()).setDatacenter(this);
        host.setActive(((GpuHostSimple)host).isActivateOnDatacenterStartup());
        return nextId;
    }
    
    @Override
    public void processEvent (final SimEvent evt) {
        if (processGpuCloudletEvents(evt) || processGpuVmEvents(evt) || 
        		processNetworkEvents(evt) || processGpuHostEvents(evt)) {
            return;
        }

        LOGGER.trace("{}: {}: Unknown event {} received.", getSimulation().clockStr(), 
        		this, evt.getTag());
    }
    
    private boolean processGpuHostEvents(final SimEvent evt) {
        if (evt.getTag() == CloudSimTag.HOST_ADD) {
            processGpuHostAdditionRequest(evt);
            return true;
        } 
        else if (evt.getTag() == CloudSimTag.HOST_REMOVE) {
            processGpuHostRemovalRequest(evt);
            return true;
        } 
        else if (evt.getTag() == CloudSimTag.HOST_POWER_ON || 
        		evt.getTag() == CloudSimTag.HOST_POWER_OFF) {
            final GpuHostSimple host = (GpuHostSimple)evt.getData();
            host.processActivation(evt.getTag() == CloudSimTag.HOST_POWER_ON);
        }

        return false;
    }
    
    //review how can process a Gpu removal request or GpuFaultInjection
    
    private void processGpuHostRemovalRequest(final SimEvent srcEvt) {
        final long hostId = (long)srcEvt.getData();
        final Host host = getHostById(hostId);
        if(Host.NULL.equals(host)) {
            LOGGER.warn(
                "{}: {}: Host {} was not found to be removed from {}.",
                getSimulation().clockStr(), getClass().getSimpleName(), hostId, this);
            return;
        }

        final var fault = new HostFaultInjection(this);
        try {
            LOGGER.error(
                "{}: {}: Host {} removed from {} due to injected failure.",
                getSimulation().clockStr(), getClass().getSimpleName(), host.getId(), this);
            fault.generateHostFault(host);
        } finally{
            fault.shutdown();
        }

        /*If the Host was found in this Datacenter, cancel the message sent to others
        * Datacenters to try to find the Host for removal.*/
        getSimulation().cancelAll(
            getSimulation().getCloudInfoService(),
            evt -> MathUtil.same(evt.getTime(), srcEvt.getTime()) &&
                   evt.getTag() == CloudSimTag.HOST_REMOVE &&
                   (long)evt.getData() == host.getId());
    }
    
    private void processGpuHostAdditionRequest(final SimEvent evt) {
        getGpuHostFromGpuHostEvent(evt).ifPresent(host -> {
            this.addHost(host);
            LOGGER.info(
                "{}: {}: Host {} added to {} during simulation runtime",
                getSimulation().clockStr(), getClass().getSimpleName(), host.getId(), this);
            //Notification must be sent only for Hosts added during simulation runtime
            notifyOnGpuHostAvailableListeners(host);
            host.getVideocard().processGpuAdditionRequest();
        });
    }
    
    private <T extends Host> void notifyOnGpuHostAvailableListeners(final T host) {
        onGpuHostAvailableListeners.forEach(listener -> listener.update(HostEventInfo.of(
        		listener, host, clock())));
    }
    
    private Optional<GpuHost> getGpuHostFromGpuHostEvent(final SimEvent evt) {
        if (evt.getData() instanceof GpuHost host) {
            return Optional.of(host);
        }

        return Optional.empty();
    }
    
    private boolean processNetworkEvents(final SimEvent evt) {
        if (evt.getTag() == CloudSimTag.ICMP_PKT_SUBMIT) {
            processPingRequest(evt);
            return true;
        }
        return false;
    }

    protected void processPingRequest(final SimEvent evt) {
        final IcmpPacket pkt = (IcmpPacket) evt.getData();
        pkt.setTag(CloudSimTag.ICMP_PKT_RETURN);
        pkt.setDestination(pkt.getSource());

        // returns the packet to the sender
        sendNow(pkt.getSource(), CloudSimTag.ICMP_PKT_RETURN, pkt);
    }
    
    private boolean processGpuVmEvents (final SimEvent evt) {
        return switch (evt.getTag()) {
            case VM_CREATE_ACK -> processGpuVmCreate(evt);
            case VM_VERTICAL_SCALING  -> requestGpuVmVerticalScaling(evt);
            case VM_DESTROY -> processGpuVmDestroy(evt, false);
            case VM_DESTROY_ACK -> processGpuVmDestroy(evt, true);
            case VM_MIGRATE -> finishGpuVmMigration(evt, false);
            case VM_MIGRATE_ACK -> finishGpuVmMigration(evt, true);
            case VM_UPDATE_CLOUDLET_PROCESSING -> updateGpuCloudletProcessing() != Double.MAX_VALUE;
            default -> false;
        };
    }
    
    protected boolean finishGpuVmMigration (final SimEvent evt, final boolean ack) {
        if (!(evt.getData() instanceof Map.Entry<?, ?>)) {
            throw new InvalidEventDataTypeException(evt, "GPUVM_MIGRATE", "Map.Entry<GpuVm, GpuHost>");
        }

        final var entry = (Map.Entry<Vm, Host>) evt.getData();

        final GpuVm vm = (GpuVm)entry.getKey();
        final GpuHost sourceHost = (GpuHost)vm.getHost();
        final GpuHost targetHost = (GpuHost)entry.getValue();

        updateGpuHostsProcessing();

        sourceHost.getVideocard().getVGpuAllocationPolicy().deallocateGpuForVGpu(vm.getVGpu());
        gpuVmAllocationPolicy.deallocateHostForVm(vm);

        targetHost.removeMigratingInVm(vm);
        final HostSuitability suitability = gpuVmAllocationPolicy.allocateHostForVm(vm, targetHost);
        final GpuSuitability gpuSuitability = targetHost.getVideocard().getVGpuAllocationPolicy()
        		.allocateGpuForVGpu(vm.getVGpu());
        
        if(suitability.fully() && gpuSuitability.fully()) {
            ((GpuVmSimple)vm).updateMigrationFinishListeners(targetHost);
            ((VGpuSimple)vm.getVGpu()).updateMigrationFinishListeners(vm.getVGpu().getGpu());
            
            vm.getBroker().getVmExecList().add(vm);

            if (ack) {
                sendNow(evt.getSource(), CloudSimTag.VM_CREATE_ACK, vm);
            }
        }

        final SimEvent event = getSimulation().findFirstDeferred(this, new PredicateType(
        		CloudSimTag.VM_MIGRATE));
        if (event == null || event.getTime() > clock()) {
            updateGpuHostsProcessing();
        }

        if (suitability.fully() && gpuSuitability.fully())
            LOGGER.info("{}: Migration of {} from {} to {} is completed.", getSimulation().clockStr(), vm, sourceHost, targetHost);
        else LOGGER.error(
            "{}: {}: Allocation of {} to the destination {} failed due to {} or {}!",
            getSimulation().clockStr(), this, vm, targetHost, suitability, gpuSuitability);

        onGpuVmMigrationFinishListeners.forEach(
        		listener -> listener.update(DatacenterVmMigrationEventInfo.of(
        				listener, vm, suitability)));
        return true;
    }
    
    protected double updateGpuCloudletProcessing () {
        if (!isTimeToUpdateGpuCloudletsProcessing()){
            return Double.MAX_VALUE;
        }

        double nextSimulationDelay = updateGpuHostsProcessing();

        if (nextSimulationDelay != Double.MAX_VALUE) {
            nextSimulationDelay = getGpuCloudletProcessingUpdateInterval(nextSimulationDelay);
            schedule(nextSimulationDelay, CloudSimTag.VM_UPDATE_CLOUDLET_PROCESSING);
        }
        setLastProcessTime(clock());

        checkIfGpuVmMigrationsAreNeeded();
        return nextSimulationDelay;
    }
    
    //need review for vgpu migration
    private void checkIfGpuVmMigrationsAreNeeded() {
        if (!isTimeToSearchForSuitableGpuHosts()) {
            return;
        }
        lastMigrationMap = (Map)gpuVmAllocationPolicy.getOptimizedAllocationMap(getGpuVmList());
        for (final Map.Entry<GpuVm, GpuHost> entry : lastMigrationMap.entrySet()) {
            requestVmMigration(entry.getKey(), entry.getValue());
        }

        if(areThereUnderOrOverloadedHostsAndMigrationIsSupported()){
            lastUnderOrOverloadedDetection = clock();
        }
    }
    
    private <T extends Vm> List<T> getGpuVmList() {
        return (List<T>) Collections.unmodifiableList(
                getHostList()
                    .stream()
                    .map(Host::getVmList)
                    .flatMap(List::stream)
                    .collect(toList()));
    }

    private boolean areThereUnderOrOverloadedHostsAndMigrationIsSupported () {
        /*if(gpuVmAllocationPolicy instanceof VmAllocationPolicyMigration migrationPolicy){
            return migrationPolicy.areHostsUnderOrOverloaded();
        }*/

        return false;
    }

    private boolean isTimeToSearchForSuitableGpuHosts(){
        final double elapsedSecs = clock() - lastUnderOrOverloadedDetection;
        return isMigrationsEnabled() && elapsedSecs >= gpuhostSearchRetryDelay;
    }

    
    protected final void setLastProcessTime(final double lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }
    
    protected double updateGpuHostsProcessing () {
        double nextSimulationDelay = Double.MAX_VALUE;
        for (final Host host : getHostList()) {
        	((GpuHost)host).getVideocard().updateGpusProcessing();
            final double delay = host.updateProcessing(clock());
            nextSimulationDelay = Math.min(delay, nextSimulationDelay);
        }

        // Guarantees a minimal interval before scheduling the event
        final double minTimeBetweenEvents = getSimulation().getMinTimeBetweenEvents()+0.01;
        nextSimulationDelay = nextSimulationDelay == 0 ? nextSimulationDelay : Math.max(nextSimulationDelay, minTimeBetweenEvents);

        return nextSimulationDelay;
    }
    
    @Override
    public <T extends Host> List<T> getHostList () {
        return (List<T>)Collections.unmodifiableList(gpuhostList);
    }
    
    private boolean isTimeToUpdateGpuCloudletsProcessing () {
        return clock() < 0.111 ||
               clock() >= lastProcessTime + getSimulation().getMinTimeBetweenEvents();
    }
    
    protected boolean processGpuVmDestroy (final SimEvent evt, final boolean ack) {
        final var vm = (GpuVm) evt.getData();
        
        String gpuTaskWarningmsg = ((GpuHost)vm.getHost()).getVideocard().processVGpuDestroy(
        		vm.getVGpu());
        
        gpuVmAllocationPolicy.deallocateHostForVm(vm);

        if (ack) {
            sendNow(vm.getBroker(), CloudSimTag.VM_DESTROY_ACK, vm);
        }

        vm.getBroker().requestShutdownWhenIdle();
        if (getSimulation().isAborted() || getSimulation().isAbortRequested()) {
            return true;
        }

        final String warningMsg = generateNotFinishedGpuCloudletsWarning (vm);
        final String msg = String.format(
                "%s: %s: %s destroyed on %s. %s. %s",
                getSimulation().clockStr(), getClass().getSimpleName(), vm, vm.getHost(), 
                gpuTaskWarningmsg, warningMsg);
        if ((warningMsg.isEmpty() && gpuTaskWarningmsg.isEmpty()) || 
        		getSimulation().isTerminationTimeSet())
            LOGGER.info(msg);
        else LOGGER.warn(msg);
        return true;
    }
    
    private String generateNotFinishedGpuCloudletsWarning (final GpuVm vm) {
        final int cloudletsNoFinished = vm.getCloudletScheduler().getCloudletList().size();
        if(cloudletsNoFinished == 0) {
            return "";
        }

        return String.format(
                "It had a total of %d cloudlets (running + waiting). %s", cloudletsNoFinished,
                "Some events may have been missed. You can try: " +
                "(a) decreasing CloudSim's minTimeBetweenEvents " + 
                "and/or Datacenter's schedulingInterval attribute; " +
                "(b) increasing broker's Vm destruction delay for idle VMs if you set it to zero; " +
                "(c) defining Cloudlets with smaller length (" + 
                "your Datacenter's scheduling interval may be smaller " + 
                "than the time to finish some Cloudlets).");
    }
    
    //need add VerticalVGpuScalling
    private boolean requestGpuVmVerticalScaling(final SimEvent evt) {
        if (evt.getData() instanceof VerticalVmScaling scaling) {
            return gpuVmAllocationPolicy.scaleVmVertically(scaling);
        }

        throw new InvalidEventDataTypeException(evt, "VM_VERTICAL_SCALING", "VerticalVmScaling");
    }
    
    private boolean processGpuVmCreate (final SimEvent evt) {
        final var vm = (GpuVm) evt.getData();
        final boolean gpuHostAllocatedForGpuVm = gpuVmAllocationPolicy.allocateHostForVm(vm).fully();
        boolean gpuAllocatedForVGpu = false;
        if (gpuHostAllocatedForGpuVm) {
        	gpuAllocatedForVGpu = ((GpuHost)vm.getHost()).getVideocard().processVGpuCreate(
        			vm.getVGpu());
            vm.updateProcessing(vm.getHost().getVmScheduler().getAllocatedMips(vm));
        }

        send(vm.getBroker(), getSimulation().getMinTimeBetweenEvents(), CloudSimTag.VM_CREATE_ACK, vm);
        return gpuHostAllocatedForGpuVm && gpuAllocatedForVGpu;
    }
    
    private boolean processGpuCloudletEvents (final SimEvent evt) {
        return switch (evt.getTag()) {
            // New Cloudlet arrives
            case CLOUDLET_SUBMIT -> processGpuCloudletSubmit(evt, false);
            // New Cloudlet arrives, but the sender asks for an ack
            case CLOUDLET_SUBMIT_ACK -> processGpuCloudletSubmit(evt, true);
            // Cancels a previously submitted Cloudlet
            case CLOUDLET_CANCEL -> processGpuCloudlet(evt, CloudSimTag.CLOUDLET_CANCEL);
            // Pauses a previously submitted Cloudlet
            case CLOUDLET_PAUSE -> processGpuCloudlet(evt, CloudSimTag.CLOUDLET_PAUSE);
            // Pauses a previously submitted Cloudlet, but the sender asks for an acknowledgement
            case CLOUDLET_PAUSE_ACK -> processGpuCloudlet(evt, CloudSimTag.CLOUDLET_PAUSE_ACK);
            // Resumes a previously submitted Cloudlet
            case CLOUDLET_RESUME -> processGpuCloudlet(evt, CloudSimTag.CLOUDLET_RESUME);
            // Resumes a previously submitted Cloudlet, but the sender asks for an acknowledgement
            case CLOUDLET_RESUME_ACK -> processGpuCloudlet(evt, CloudSimTag.CLOUDLET_RESUME_ACK);
            default -> false;
        };
    }
    
    protected boolean processGpuCloudlet (final SimEvent evt, final CloudSimTag tag) {
        final GpuCloudlet cloudlet;
        try {
            cloudlet = (GpuCloudlet) evt.getData();
        } 
        catch (ClassCastException e) {
            LOGGER.error("{}: Error in processing Cloudlet: {}", super.getName(), e.getMessage());
            return false;
        }

        return switch (tag) {
            case CLOUDLET_CANCEL -> processGpuCloudletCancel(cloudlet);
            case CLOUDLET_PAUSE -> processGpuCloudletPause(cloudlet, false);
            case CLOUDLET_PAUSE_ACK -> processGpuCloudletPause(cloudlet, true);
            case CLOUDLET_RESUME -> processGpuCloudletResume(cloudlet, false);
            case CLOUDLET_RESUME_ACK -> processGpuCloudletResume(cloudlet, true);
            default -> {
                LOGGER.trace(
                    "{}: Unable to handle a request from {} with event tag = {}",
                    this, evt.getSource().getName(), evt.getTag());
                yield false;
            }
        };
    }
    
    protected boolean processGpuCloudletResume (final GpuCloudlet cloudlet, final boolean ack) {
        final double cloudletEstimatedFinishTime = cloudlet.getVm().getCloudletScheduler()
        		.cloudletResume(cloudlet);
        
        final double gpuTaskEstimatedFinishTime = cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler()
        		.gpuTaskResume(cloudlet.getGpuTask());

        final double estimatedFinishTime = cloudletEstimatedFinishTime + gpuTaskEstimatedFinishTime;
        
        if (estimatedFinishTime > 0.0 && estimatedFinishTime > clock()) {
            schedule(this,
                getGpuCloudletProcessingUpdateInterval(estimatedFinishTime),
                CloudSimTag.VM_UPDATE_CLOUDLET_PROCESSING);
        }

        sendAck(ack, cloudlet, CloudSimTag.CLOUDLET_RESUME_ACK);
        return true;
    }
    
    protected boolean processGpuCloudletPause (final GpuCloudlet cloudlet, final boolean ack) {
        cloudlet.getVm().getCloudletScheduler().cloudletPause(cloudlet);
        cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler().gpuTaskPause(cloudlet.getGpuTask());
        sendAck(ack, cloudlet, CloudSimTag.CLOUDLET_PAUSE_ACK);
        return true;
    }
    
    private void sendAck (final boolean ack, final GpuCloudlet cloudlet, final CloudSimTag tag) {
        if (ack) {
            sendNow(cloudlet.getBroker(), tag, cloudlet);
        }
    }

    
    protected boolean processGpuCloudletCancel (final GpuCloudlet cloudlet) {
        cloudlet.getVm().getCloudletScheduler().cloudletCancel(cloudlet);
        cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler().gpuTaskCancel(cloudlet.getGpuTask());
        sendNow(cloudlet.getBroker(), CloudSimTag.CLOUDLET_CANCEL, cloudlet);
        return true;
    }
    
    protected boolean processGpuCloudletSubmit (final SimEvent evt, final boolean ack) {
        final var cloudlet = (GpuCloudlet) evt.getData();
        if (cloudlet.isFinished() && cloudlet.getGpuTask().isFinished()) {
            notifyBrokerAboutAlreadyFinishedGpuCloudlet(cloudlet, ack);
            return false;
        }

        submitGpuCloudletToGpuVm(cloudlet, ack);
        return true;
    }
    
    private void submitGpuCloudletToGpuVm (final GpuCloudlet cloudlet, final boolean ack) {
    	
        final double fileTransferTime = getDatacenterStorage().predictFileTransferTime(
        		Stream.concat(cloudlet.getRequiredFiles().stream(), 
        				cloudlet.getGpuTask().getRequiredFiles().stream()).toList());

        final var cloudletScheduler = cloudlet.getVm().getCloudletScheduler();
        final double estimatedCloudletFinishTime = cloudletScheduler.cloudletSubmit(cloudlet, 
        		fileTransferTime);
        
        final var gpuTaskScheduler = cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler();
        final double estimatedGpuTaskFinishTime = gpuTaskScheduler.gpuTaskSubmit(cloudlet.getGpuTask(), 
        		fileTransferTime);
        
        final double estimatedFinishTime = estimatedCloudletFinishTime + 
        		estimatedGpuTaskFinishTime;
        // if this cloudlet is in the exec queue
        if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
            send(this,
                getGpuCloudletProcessingUpdateInterval(estimatedFinishTime),
                CloudSimTag.VM_UPDATE_CLOUDLET_PROCESSING);
        }
        
        ((CustomerEntityAbstract)cloudlet).setCreationTime();
        sendGpuCloudletSubmitAckToBroker(cloudlet, ack);
    }
    
    protected double getGpuCloudletProcessingUpdateInterval (
    		final double nextFinishingGpuCloudletTime) {
        if(schedulingInterval == 0) {
            return nextFinishingGpuCloudletTime;
        }

        final double time = Math.floor(clock());
        final double mod = time % schedulingInterval;
        
        final double delay = mod == 0 ? schedulingInterval : 
        	(time - mod + schedulingInterval) - time;
        return Math.min(nextFinishingGpuCloudletTime, delay);
    }
    
    private double clock() {
        return getSimulation().clock();
    }
    
    private void notifyBrokerAboutAlreadyFinishedGpuCloudlet (final GpuCloudlet cloudlet, 
    		final boolean ack) {
        LOGGER.warn(
            "{}: {} owned by {} is already completed/finished. It won't be executed again.",
            getName(), cloudlet, cloudlet.getBroker());

        sendGpuCloudletSubmitAckToBroker(cloudlet, ack);

        sendNow(cloudlet.getBroker(), CloudSimTag.CLOUDLET_RETURN, cloudlet);
    }
    
    private void sendGpuCloudletSubmitAckToBroker (final GpuCloudlet cloudlet, 
    		final boolean ack) {
        if(!ack){
            return;
        }

        sendNow(cloudlet.getBroker(), CloudSimTag.CLOUDLET_SUBMIT_ACK, cloudlet);
    }
    
    @Override
    public double getHostSearchRetryDelay() {
        return gpuhostSearchRetryDelay;
    }

    @Override
    public Datacenter setHostSearchRetryDelay(final double delay) {
        if(delay == 0){
            throw new IllegalArgumentException("gpuHostSearchRetryDelay cannot be 0. Set a positive "
            		+ "value to define an actual delay or a negative value to indicate a new Host "
            		+ "search must be tried as soon as possible.");
        }

        this.gpuhostSearchRetryDelay = delay;
        return this;
    }
    
    @Override
    public PowerModelDatacenter getPowerModel() {
        return powerModel;
    }

    @Override
    public final void setPowerModel(final PowerModelDatacenter powerModel) {
        requireNonNull(powerModel,
            "powerModel cannot be null. You could provide a " +
            PowerModelDatacenter.class.getSimpleName() + ".NULL instead");

        if (powerModel.getDatacenter() != null && 
        		powerModel.getDatacenter() != Datacenter.NULL && 
        		!this.equals(powerModel.getDatacenter())){
            throw new IllegalStateException("The given PowerModel is already assigned to another "
            		+ "Datacenter. Each Datacenter must have its own PowerModel instance.");
        }

        this.powerModel = powerModel;
    }
    
    @Override
    public boolean isMigrationsEnabled() {
        return migrationsEnabled && gpuVmAllocationPolicy.isVmMigrationSupported();
    }

    @Override
    public final Datacenter enableMigrations() {
        if(!gpuVmAllocationPolicy.isVmMigrationSupported()){
            LOGGER.warn(
                "{}: {}: It was requested to enable VM migrations but the {} doesn't support that.",
                getSimulation().clockStr(), getName(), 
                gpuVmAllocationPolicy.getClass().getSimpleName());
            return this;
        }

        this.migrationsEnabled = true;
        return this;
    }

    @Override
    public final Datacenter disableMigrations() {
        this.migrationsEnabled = false;
        return this;
    }
    
    @Override
    public DatacenterStorage getDatacenterStorage() {
        return this.datacenterStorage;
    }

    @Override
    public final void setDatacenterStorage(final DatacenterStorage datacenterStorage) {
        datacenterStorage.setDatacenter(this);
        this.datacenterStorage = datacenterStorage;
    }
    
    @Override
    public void requestVmMigration(final Vm sourceVm) {
        requestVmMigration(sourceVm, Host.NULL);
    }

    @Override
    public void requestVmMigration(final Vm sourceVm, Host targetHost) {
        if(GpuHost.NULL.equals(targetHost)){
            targetHost = gpuVmAllocationPolicy.findHostForVm((GpuVm)sourceVm).orElse(GpuHost.NULL);
        }

        if(GpuHost.NULL.equals(targetHost)) {
            LOGGER.warn("{}: {}: No suitable host found for {} in {}", 
            		sourceVm.getSimulation().clockStr(), getClass().getSimpleName(), sourceVm, this);
            return;
        }

        final GpuHost sourceHost = (GpuHost)sourceVm.getHost();
        final double delay = timeToMigrateGpuVm((GpuVm)sourceVm, (GpuHost)targetHost);
        final String msg1 =
            GpuHost.NULL.equals(sourceHost) ?
                String.format("%s to %s", sourceVm, targetHost) :
                String.format("%s from %s to %s", sourceVm, sourceHost, targetHost);

        final String currentTime = getSimulation().clockStr();
        final var fmt = "It's expected to finish in %.2f seconds, considering the %.0f%% of "
        		+ "bandwidth allowed for migration and the GpuVM RAM size.";
        final String msg2 = String.format(fmt, delay, getBandwidthPercentForMigration()*100);
        LOGGER.info("{}: {}: Migration of {} is started. {}", currentTime, getName(), msg1, msg2);

        if(targetHost.addMigratingInVm(sourceVm)) {
            sourceHost.addVmMigratingOut(sourceVm);
            ((GpuVm)sourceVm).getVGpu().getGpu().addVGpuMigratingOut(((GpuVm)sourceVm).getVGpu());
            send(this, delay, CloudSimTag.VM_MIGRATE, new TreeMap.SimpleEntry<>(sourceVm, targetHost));
        }
    }
    
    private double timeToMigrateGpuVm (final GpuVm vm, final GpuHost targetHost) {
        return vm.getRam().getCapacity() / bitesToBytes(targetHost.getBw().getCapacity() * 
        		getBandwidthPercentForMigration());
    }

    
    @Override
    public void shutdown() {
        super.shutdown();
        LOGGER.info("{}: {} is shutting down...", getSimulation().clockStr(), getName());
    }

    @Override
    protected void startInternal() {
        LOGGER.info("{}: {} is starting...", getSimulation().clockStr(), getName());
        gpuhostList.stream()
                .filter(not(Host::isActive))
                .map(host -> (GpuHostSimple)host)
                .forEach(host -> host.setActive(host.isActivateOnDatacenterStartup()));
        sendNow(getSimulation().getCloudInfoService(), CloudSimTag.DC_REGISTRATION_REQUEST, this);
    }

    @Override
    public Stream<? extends Host> getActiveHostStream() {
        return gpuhostList.stream().filter(Host::isActive);
    }

    @Override
    public DatacenterCharacteristics getCharacteristics() {
        return characteristics;
    }

    @Override
    public GpuVmAllocationPolicy getVmAllocationPolicy() {
        return gpuVmAllocationPolicy;
    }
    
    @Override
    public double getSchedulingInterval() {
        return schedulingInterval;
    }

    @Override
    public final Datacenter setSchedulingInterval(final double schedulingInterval) {
        this.schedulingInterval = Math.max(schedulingInterval, 0);
        return this;
    }
    
    @Override
    public double getTimeZone() {
        return timeZone;
    }

    @Override
    public final Datacenter setTimeZone(final double timeZone) {
        this.timeZone = validateTimeZone(timeZone);
        return this;
    }
    
    @Override
    public Host getHost(final int index) {
        if (index >= 0 && index < getHostList().size()) {
            return getHostList().get(index);
        }

        return Host.NULL;
    }

    @Override
    public long getActiveHostsNumber(){
        return activeHostsNumber;
    }
    
    public void updateActiveGpuHostsNumber(final GpuHost host){
        activeHostsNumber += host.isActive() ? 1 : -1;
    }

    @Override
    public long size() {
        return gpuhostList.size();
    }

    @Override
    public Host getHostById(final long id) {
        return gpuhostList.stream().filter(host -> host.getId() == id).findFirst()
        		.map(host -> (GpuHost)host).orElse(GpuHost.NULL);
    }
    
    @Override
    public <T extends Host> Datacenter addHostList(final List<T> hostList) {
        requireNonNull(hostList);
        hostList.forEach(this::addHost);
        return this;
    }

    @Override
    public <T extends Host> Datacenter addHost(final T host) {
        if(gpuVmAllocationPolicy == null || gpuVmAllocationPolicy == gpuVmAllocationPolicy.NULL) {
            throw new IllegalStateException("A GpuVmAllocationPolicy must be set before adding a "
            		+ "new Host to the Datacenter.");
        }

        setupGpuHost((GpuHost)host, getLastGpuHostId());
        ((List<T>)gpuhostList).add(host);
        return this;
    }
    
    @Override
    public <T extends Host> Datacenter removeHost(final T host) {
        gpuhostList.remove(host);
        return this;
    }

    @Override
    public String toString() {
        return String.format("Datacenter %d", getId());
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        final GpuDatacenterSimple that = (GpuDatacenterSimple) object;

        return !characteristics.equals(that.characteristics);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + characteristics.hashCode();
        return result;
    }
    
    @Override
    public double getBandwidthPercentForMigration() {
        return bandwidthPercentForMigration;
    }

    @Override
    public void setBandwidthPercentForMigration(final double bandwidthPercentForMigration) {
        if(bandwidthPercentForMigration <= 0){
            throw new IllegalArgumentException("The bandwidth migration percentage must be greater "
            		+ "than 0.");
        }

        if(bandwidthPercentForMigration > 1){
            throw new IllegalArgumentException("The bandwidth migration percentage must be lower "
            		+ "or equal to 1.");
        }

        this.bandwidthPercentForMigration = bandwidthPercentForMigration;
    }
    
    @Override
    public Datacenter addOnHostAvailableListener(final EventListener<HostEventInfo> listener) {
        onGpuHostAvailableListeners.add(requireNonNull(listener));
        return this;
    }

    @Override
    public Datacenter addOnVmMigrationFinishListener(final EventListener<DatacenterVmMigrationEventInfo> listener) {
        onGpuVmMigrationFinishListeners.add(requireNonNull(listener));
        return this;
    }

}
