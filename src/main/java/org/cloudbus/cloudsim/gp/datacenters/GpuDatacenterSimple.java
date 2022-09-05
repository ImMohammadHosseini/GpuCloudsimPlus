package org.cloudbus.cloudsim.gp.datacenters;

import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.CloudSimTag;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.resources.SanStorage;
import org.cloudbus.cloudsim.core.CustomerEntityAbstract;
import org.cloudbus.cloudsim.resources.DatacenterStorage;
import org.cloudbus.cloudsim.power.models.PowerModelDatacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.power.models.PowerModelDatacenterSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristicsSimple;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;
import org.cloudbus.cloudsim.gp.hosts.GpuHostSimple;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudlet;
import org.cloudbus.cloudsim.gp.allocationpolicies.GpuVmAllocationPolicy;
import org.cloudbus.cloudsim.gp.allocationpolicies.GpuVmAllocationPolicySimple;

import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.HostEventInfo;

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
    private final List<EventListener<DatacenterVmMigrationEventInfo>> onVmMigrationFinishListeners;

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
        setHostList(hostList);
        setLastProcessTime(0.0);
        setSchedulingInterval(0);
        setDatacenterStorage(storage);
        setPowerModel(new PowerModelDatacenterSimple(this));

        this.onGpuHostAvailableListeners = new ArrayList<>();
        this.onVmMigrationFinishListeners = new ArrayList<>();
        this.characteristics = new DatacenterCharacteristicsSimple(this);
        this.bandwidthPercentForMigration = DEF_BW_PERCENT_FOR_MIGRATION;
        this.migrationsEnabled = true;
        this.gpuhostSearchRetryDelay = -1;

        this.lastMigrationMap = Collections.emptyMap();

        setVmAllocationPolicy(gpuVmAllocationPolicy);
    }
    
    private void setHostList (final List<? extends GpuHost> hostList) {
        this.gpuhostList = requireNonNull(hostList);
        setupGpuHosts();
    }
//need review
    
    private void setupGpuHosts () {
        long lastGpuHostId = getLastGpuHostId();
        for (final GpuHost host : gpuhostList)
            lastGpuHostId = setupGpuHost (host, lastGpuHostId);
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

        LOGGER.trace("{}: {}: Unknown event {} received.", getSimulation().clockStr(), this, evt.getTag());
    }
    
    private boolean processGpuVmEvents (final SimEvent evt) {
        return switch (evt.getTag()) {
            case VM_CREATE_ACK -> processGpuVmCreate(evt);
            case VM_VERTICAL_SCALING  -> requestVmVerticalScaling(evt);
            case VM_DESTROY -> processVmDestroy(evt, false);
            case VM_DESTROY_ACK -> processVmDestroy(evt, true);
            case VM_MIGRATE -> finishVmMigration(evt, false);
            case VM_MIGRATE_ACK -> finishVmMigration(evt, true);
            case VM_UPDATE_CLOUDLET_PROCESSING -> updateCloudletProcessing() != Double.MAX_VALUE;
            default -> false;
        };
    }
    
    private boolean processGpuVmCreate (final SimEvent evt) {
        final var vm = (GpuVm) evt.getData();
        hhhh
        // TODO
        final boolean gpuHostAllocatedForGpuVm = gpuVmAllocationPolicy.allocateHostForVm(vm).fully();
        if (hostAllocatedForVm) {
            vm.updateProcessing(vm.getHost().getVmScheduler().getAllocatedMips(vm));
        }

        /* Acknowledges that the request was received by the Datacenter,
          (the broker is expecting that if the Vm was created or not). */
        send(vm.getBroker(), getSimulation().getMinTimeBetweenEvents(), CloudSimTag.VM_CREATE_ACK, vm);

        return hostAllocatedForVm;
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
}
