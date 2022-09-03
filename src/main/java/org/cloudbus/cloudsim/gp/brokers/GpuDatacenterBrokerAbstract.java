package org.cloudbus.cloudsim.gp.brokers;

import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;
import org.cloudbus.cloudsim.gp.resources.CustomVGpuSimple;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenter;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudlet;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.gp.core.GpuCloudsimTags;
import org.cloudbus.cloudsim.gp.vms.GpuVmSimple;
import org.cloudbus.cloudsim.gp.vms.GpuVm;

import org.cloudbus.cloudsim.util.InvalidEventDataTypeException;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.core.events.CloudSimEvent;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public abstract class GpuDatacenterBrokerAbstract extends CloudSimEntity implements 
GpuDatacenterBroker {
	
	private static final Function<GpuVm, Double> DEF_VM_DESTRUCTION_DELAY_FUNC = 
			gpuvm -> DEF_VM_DESTRUCTION_DELAY;
	private static final Function<CustomVGpu, Double> DEF_VGPU_DESTRUCTION_DELAY_FUNC = 
			vgpu -> DEF_VGPU_DESTRUCTION_DELAY;
	
    private boolean selectClosestGpuDatacenter;
    private final List<EventListener<DatacenterBrokerEventInfo>> onGpuVmsCreatedListeners;
    private final List<EventListener<DatacenterBrokerEventInfo>> onVGpusCreatedListeners;
    private GpuVm lastSelectedGpuVm;
    private GpuDatacenter lastSelectedGpuDc;
    private double failedGpuVmsRetryDelay;

    private final List<GpuVm> gpuvmFailedList;
    private final List<GpuVm> gpuvmWaitingList;
    private final List<GpuVm> gpuvmExecList;
    private final List<GpuVm> gpuvmCreatedList;

    private final List<GpuCloudlet> gpucloudletWaitingList;
    private final List<GpuCloudlet> gpucloudletSubmittedList;
    private final List<GpuCloudlet> gpucloudletsFinishedList;
    private final List<GpuCloudlet> gpucloudletsCreatedList;

    private boolean wereThereWaitingGpuCloudlets;

    private BiFunction<GpuDatacenter, GpuVm, GpuDatacenter> gpudatacenterMapper;

    private Function<GpuCloudlet, GpuVm> gpuvmMapper;

    private Comparator<GpuVm> gpuvmComparator;
    private Comparator<GpuCloudlet> gpucloudletComparator;

    private int gpuvmCreationRequests;

    private List<GpuDatacenter> gpudatacenterList;

    private GpuCloudlet lastSubmittedGpuCloudlet;
    private GpuVm lastSubmittedGpuVm;

    private Function<GpuVm, Double> gpuvmDestructionDelayFunction;
    private Function<CustomVGpu, Double> vgpuDestructionDelayFunction;

    private boolean shutdownRequested;
    private boolean shutdownWhenIdle;
    private boolean gpuvmCreationRetrySent;
    
	public GpuDatacenterBrokerAbstract (final CloudSim simulation, final String name) {
		super (simulation);
		if(!name.isEmpty()) {
            setName(name);
        }

        this.onGpuVmsCreatedListeners = new ArrayList<>();
        this.onVGpusCreatedListeners = new ArrayList<>();
        this.lastSubmittedGpuCloudlet = GpuCloudlet.NULL;
        this.lastSubmittedGpuVm = GpuVm.NULL;
        this.lastSelectedGpuVm = GpuVm.NULL;
        this.lastSelectedGpuDc = GpuDatacenter.NULL;
        this.shutdownWhenIdle = true;

        this.gpuvmCreationRequests = 0;
        this.failedGpuVmsRetryDelay = 5;
        this.gpuvmFailedList = new ArrayList<>();
        this.gpuvmWaitingList = new ArrayList<>();
        this.gpuvmExecList = new ArrayList<>();
        this.gpuvmCreatedList = new ArrayList<>();
        this.gpucloudletWaitingList = new ArrayList<>();
        this.gpucloudletsFinishedList = new ArrayList<>();
        this.gpucloudletsCreatedList = new ArrayList<>();
        this.gpucloudletSubmittedList = new ArrayList<>();
        setDatacenterList(new ArrayList<>());

        setDatacenterMapper(this::defaultGpuDatacenterMapper);
        setVmMapper(this::defaultGpuVmMapper);
        gpuvmDestructionDelayFunction = DEF_VM_DESTRUCTION_DELAY_FUNC;
        vgpuDestructionDelayFunction = DEF_VGPU_DESTRUCTION_DELAY_FUNC;

    }
	
	@Override
    public final GpuDatacenterBroker setSelectClosestDatacenter (final boolean select) {
        this.selectClosestGpuDatacenter = select;
        if(select){
            setDatacenterMapper(this::closestDatacenterMapper);
        }

        return this;
    }
	
	@Override
    public final DatacenterBroker setDatacenterMapper(final BiFunction<Datacenter, Vm, 
    		Datacenter> datacenterMapper) {
        this.gpudatacenterMapper = requireNonNull(datacenterMapper);
        return this;
    }
	
	@Override
    public boolean isSelectClosestDatacenter () {
        return selectClosestGpuDatacenter;
    }

    @Override
    public GpuDatacenterBroker submitVmList (final List<? extends Vm> list, 
    		final double submissionDelay) {
        setDelayForEntitiesWithNoDelay(list, submissionDelay);
        return submitVmList (list);
    }
    
    @Override
    public GpuDatacenterBroker submitVmList(final List<? extends Vm> list) {
        sortVmsIfComparatorIsSet(list);
        configureEntities(list);
        lastSubmittedGpuVm = setIdForEntitiesWithoutOne(list, lastSubmittedGpuVm);
        gpuvmWaitingList.addAll((List<GpuVm>)(List<?>) list);

        if (isStarted() && !list.isEmpty()) {
            LOGGER.info(
                "{}: {}: List of {} GPUVMs submitted to the broker during simulation execution. "
                + "GPUVMs creation request sent to Datacenter.",
                getSimulation().clockStr(), getName(), list.size());
            requestDatacentersToCreateWaitingCloudlets();
            if(!gpuvmCreationRetrySent) {
                lastSelectedGpuDc = null;
                requestDatacenterToCreateWaitingVms(false, false);
            }
        }

        return this;
    }
    
    private void configureEntities (final List<? extends CustomerEntity> customerEntities) {
        for (final CustomerEntity entity : customerEntities) {
            entity.setBroker(this);
            entity.setArrivedTime(getSimulation().clock());
            /*if(entity instanceof VmGroup vmGroup) {
                configureEntities(vmGroup.getVmList());
            }*/
        }
    }
    
    private <T extends CustomerEntity> T setIdForEntitiesWithoutOne (
    		final List<? extends T> list, T lastSubmittedEntity) {
        return Simulation.setIdForEntitiesWithoutOne(list, lastSubmittedEntity);
    }

    private void sortVmsIfComparatorIsSet (final List<? extends Vm> list) {
        if (gpuvmComparator != null) {
        	//list = (List<GpuVm>)(List<?>) list;
        	((List<GpuVm>)(List<?>)list).sort(gpuvmComparator);
        }
    }
    

    @Override
    public GpuDatacenterBroker submitVm (final Vm vm) {
        requireNonNull(vm);
        if (GpuVm.NULL.equals(vm)) {
            return this;
        }

        final ArrayList<Vm> newVmList = new ArrayList<Vm>(1);
        newVmList.add(vm);
        return submitVmList(newVmList);
    }

    @Override
    public GpuDatacenterBroker submitCloudlet (final Cloudlet cloudlet) {
        requireNonNull(cloudlet);
        if (cloudlet == GpuCloudlet.NULL) {
            return this;
        }

        final ArrayList<Cloudlet> newCloudletList = new ArrayList<Cloudlet>(1);
        newCloudletList.add(cloudlet);
        return submitCloudletList(newCloudletList);
    }

    @Override
    public GpuDatacenterBroker submitCloudletList (final List<? extends Cloudlet> list, 
    		double submissionDelay) {
        return submitCloudletList(list, Vm.NULL, submissionDelay);
    }

    @Override
    public GpuDatacenterBroker submitCloudletList (final List<? extends Cloudlet> list, Vm vm) {
        return submitCloudletList(list, vm, -1);
    }

    @Override
    public GpuDatacenterBroker submitCloudletList (final List<? extends Cloudlet> list, 
    		Vm vm, double submissionDelay) {
        setDelayForEntitiesWithNoDelay(list, submissionDelay);
        bindCloudletsToVm(list, vm);
        return submitCloudletList(list);
    }
    
    @Override
    public GpuDatacenterBroker submitCloudletList(final List<? extends Cloudlet> list) {
        if (list.isEmpty()) {
            return this;
        }

        sortGpuCloudletsIfComparatorIsSet((List<GpuCloudlet>)(List<?>)list);
        configureEntities(list);
        lastSubmittedGpuCloudlet = setIdForEntitiesWithoutOne(list, lastSubmittedGpuCloudlet);
        gpucloudletSubmittedList.addAll((List<GpuCloudlet>)(List<?>)list);
        setSimulationForGpuCloudletUtilizationModels((List<GpuCloudlet>)(List<?>)list);
        gpucloudletWaitingList.addAll((List<GpuCloudlet>)(List<?>)list);
        wereThereWaitingGpuCloudlets = true;

        if (!isStarted()) {
            return this;
        }

        LOGGER.info(
            "{}: {}: List of {} GpuCloudlets submitted to the broker during simulation execution.",
            getSimulation().clockStr(), getName(), list.size());

        LOGGER.info("Cloudlets creation request sent to Datacenter.");
        requestDatacentersToCreateWaitingCloudlets();

        return this;
    }
    
    @Override
    public boolean bindCloudletToVm(final Cloudlet cloudlet, final Vm vm) {
        if (!this.equals(cloudlet.getBroker()) && 
        		!DatacenterBroker.NULL.equals(cloudlet.getBroker())) {
            return false;
        }
        cloudlet.setVm(vm);
        return bindGpuTaskToVGpu (((GpuCloudlet)cloudlet).getGpuTask(), ((GpuVm)vm).getVGpu());
    }
    
    @Override
    public boolean bindGpuTaskToVGpu (GpuTask gpuTask, CustomVGpu vgpu) {
    	gpuTask.setVGpu (vgpu);
    	return true;
    }
    
    private void bindCloudletsToVm (final List<? extends Cloudlet> cloudlets, Vm vm) {
        if (Vm.NULL.equals(vm)) {
            return;
        }

        cloudlets.forEach(c -> c.setVm(vm));
        ((List<GpuCloudlet>)(List<?>)cloudlets).forEach(
        		c -> c.getGpuTask().setVGpu(((GpuVm)vm).getVGpu()));
    }

    private void sortGpuCloudletsIfComparatorIsSet (final List<? extends GpuCloudlet> cloudlets) {
        if (gpucloudletComparator != null) {
            cloudlets.sort(gpucloudletComparator);
        }
    }

    private void setSimulationForGpuCloudletUtilizationModels (
    		final List<? extends GpuCloudlet> cloudletList) {
        for (final GpuCloudlet cloudlet : cloudletList) {
            setSimulationForUtilizationModel(cloudlet.getUtilizationModelCpu());
            setSimulationForUtilizationModel(cloudlet.getUtilizationModelBw());
            setSimulationForUtilizationModel(cloudlet.getUtilizationModelRam());
            
            setSimulationForUtilizationModel(cloudlet.getGpuTask().getUtilizationModelGpu());
            setSimulationForUtilizationModel(cloudlet.getGpuTask().getUtilizationModelBw());
            setSimulationForUtilizationModel(cloudlet.getGpuTask().getUtilizationModelGddram());
        }
    }

    private void setSimulationForUtilizationModel (
    		final UtilizationModel utilizationModel) {
        if (utilizationModel.getSimulation() == null || 
        		utilizationModel.getSimulation() == Simulation.NULL) {
            utilizationModel.setSimulation(getSimulation());
        }
    }
    
    private void setDelayForEntitiesWithNoDelay(final List<? extends CustomerEntity> entities, 
    		final double submissionDelay) {
        if (submissionDelay < 0) 
            return;
        
        entities.stream()
            .filter(entity -> entity.getSubmissionDelay() <= 0)
            .forEach(entity -> entity.setSubmissionDelay(submissionDelay));
    }
    
    @Override
    public void processEvent (final SimEvent evt) {
        if (processGpuCloudletEvents(evt) || processGpuVmEvents(evt) || processGeneralEvents(evt)) {
            return;
        }

        LOGGER.trace("{}: {}: Unknown event {} received.", getSimulation().clockStr(), this, evt.getTag());
    }
    
    private boolean processGpuVmEvents (final SimEvent evt) {
        return switch (evt.getTag()) {
            case VM_CREATE_RETRY -> {
                gpuvmCreationRetrySent = false;
                yield requestDatacenterToCreateWaitingGpuVms(false, true);
            }
            case VM_CREATE_ACK -> processGpuVmCreateResponseFromGpuDatacenter(evt);
            case VM_VERTICAL_SCALING -> requestVmVerticalScaling(evt);
            default -> false;
        };
    }
    
    private boolean processGpuVmCreateResponseFromGpuDatacenter (final SimEvent evt) {
        final var vm = (GpuVm) evt.getData();

        //if the VM was successfully created in the requested Datacenter
        if (vm.isCreated()) {
            processSuccessGpuVmCreationInGpuDatacenter(vm);
            vm.notifyOnHostAllocationListeners();
            vm.getVGpu().notifyOnGpuAllocationListeners();//maybe in Datacenter
        } 
        else {
            vm.setFailed(true);
            vm.getVGpu().setFailed(true);
            
            if (!isRetryFailedVms()) {
                gpuvmWaitingList.remove(vm);
                gpuvmFailedList.add(vm);
                LOGGER.warn("{}: {}: {} has been moved to the failed list because creation retry is "
                		+ "not enabled.", getSimulation().clockStr(), getName(), vm);
            }

            vm.notifyOnCreationFailureListeners(lastSelectedGpuDc);
            vm.getVGpu().notifyOnCreationFailureListeners();//maybe in Datacenter
        }

        //Decreases to indicate an ack for the request was received (either if the VM was created or not)
        gpuvmCreationRequests--;

        if(gpuvmCreationRequests == 0 && !gpuvmWaitingList.isEmpty()) {
            requestCreationOfWaitingVmsToFallbackDatacenter();
        }

        if(allNonDelayedVmsCreated()) {
            requestDatacentersToCreateWaitingCloudlets();
        }

        return vm.isCreated();
    }
    
    private void processSuccessGpuVmCreationInGpuDatacenter (final GpuVm vm) {
        /*if(vm instanceof VmGroup vmGroup){
            int createdVms = 0;
            for (final Vm nextVm : vmGroup.getVmList()) {
                if (nextVm.isCreated()) {
                    processSuccessVmCreationInDatacenter(nextVm);
                    createdVms++;
                }
            }

            if(createdVms == vmGroup.size()){
                vmWaitingList.remove(vmGroup);
            }

            return;
        }*/

        gpuvmWaitingList.remove(vm);
        gpuvmExecList.add(vm);
        gpuvmCreatedList.add(vm);
        notifyOnGpuVmsCreatedListeners();
        notifyOnVGpusCreatedListeners();//maybe in Datacenter
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void notifyOnGpuVmsCreatedListeners () {
        if(!gpuvmWaitingList.isEmpty()) 
            return;
        
        for (int i = 0; i < onGpuVmsCreatedListeners.size(); i++) {
            final var listener = onGpuVmsCreatedListeners.get(i);
            listener.update(DatacenterBrokerEventInfo.of(listener, this));
        }
    }
    
    //maybe in Datacenter
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void notifyOnVGpusCreatedListeners () {
        if(!gpuvmWaitingList.isEmpty()) 
            return;
        
        for (int i = 0; i < onVGpusCreatedListeners.size(); i++) {
            final var listener = onVGpusCreatedListeners.get(i);
            listener.update(DatacenterBrokerEventInfo.of(listener, this));
        }
    }
    
    @Override
    public boolean isRetryFailedVms () {
        return failedGpuVmsRetryDelay > 0;
    }
    
    private boolean processGpuCloudletEvents (final SimEvent evt) {
    	boolean process;
    	switch (evt.getTag()) {
    		case CLOUDLET_FAIL: process = processGpuCloudletFail(evt);
        	case CLOUDLET_READY: process = processGpuCloudletReady(evt);
        	case CLOUDLET_PAUSE: process = processGpuCloudletPause(evt);
        	case CLOUDLET_CANCEL: process = processGpuCloudletCancel(evt);
        	case CLOUDLET_FINISH: process = processGpuCloudletFinish(evt);
        	case CLOUDLET_RETURN: process = processGpuCloudletReturn(evt);
        	case CLOUDLET_UPDATE_ATTRIBUTES: process = executeRunnableEvent(evt);
        	default: process = false;
    	};
    	return process;
    }
    
    private boolean processGpuCloudletFinish (final SimEvent evt) {
        final var cloudlet = (GpuCloudlet)evt.getData();
        logGpuCloudletStatusChange(cloudlet, "finish running");
        
        if (cloudlet.getFinishedLengthSoFar() == 0) {
            updateHostProcessing(cloudlet);
        }
        
        if (cloudlet.getGpuTask().getFinishedLengthSoFar() == 0) {
            updateGpuProcessing(cloudlet);
        }

        if (cloudlet.getGpuTask().getFinishedLengthSoFar() == 0) {
        	cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler().gpuTaskFail(cloudlet.getGpuTask());
        	return true;
        }
        
        if(cloudlet.getFinishedLengthSoFar() == 0) {
            cloudlet.getVm().getCloudletScheduler().cloudletFail(cloudlet);
            return true;
        }
        
        final long prevCloudletLength = cloudlet.getLength();
        cloudlet.setLength(cloudlet.getFinishedLengthSoFar());
        final long prevGpuTaskLength = cloudlet.getGpuTask().getBlockLength();
        cloudlet.getGpuTask().setBlockLength(prevGpuTaskLength);

        updateHostProcessing(cloudlet);
        updateGpuProcessing(cloudlet);
        
        if(prevCloudletLength < 0 || prevGpuTaskLength < 0){
            final double delay = cloudlet.getSimulation().getMinTimeBetweenEvents();
            final Datacenter dc = cloudlet.getVm().getHost().getDatacenter();
            dc.schedule(delay, CloudSimTag.VM_UPDATE_CLOUDLET_PROCESSING);
        }
        return true;
    }
    
    private void updateHostProcessing (final GpuCloudlet cloudlet) {
        cloudlet.getVm().getHost().updateProcessing(getSimulation().clock());
    }

    private void updateGpuProcessing (final GpuCloudlet cloudlet) {
        cloudlet.getGpuTask().getVGpu().getGpu().updateProcessing(getSimulation().clock());
    }
    
    private boolean processGpuCloudletCancel (final SimEvent evt) {
        final var cloudlet = (GpuCloudlet)evt.getData();
        logGpuCloudletStatusChange(cloudlet, "cancel execution of");
        cloudlet.getVm().getCloudletScheduler().cloudletCancel(cloudlet);
        cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler().gpuTaskCancel(cloudlet.getGpuTask());
        return true;
    }

    
    private boolean processGpuCloudletFail (final SimEvent evt) {
        final var cloudlet = (GpuCloudlet)evt.getData();
        cloudlet.getVm().getCloudletScheduler().cloudletFail(cloudlet);
        cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler().gpuTaskFail(cloudlet.getGpuTask());
        return true;
    }
    
    private boolean processGpuCloudletPause (final SimEvent evt){
        final var cloudlet = (GpuCloudlet)evt.getData();
        logGpuCloudletStatusChange(cloudlet, "de-schedule (pause)");
        cloudlet.getVm().getCloudletScheduler().cloudletPause(cloudlet);
        cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler().gpuTaskPause(cloudlet.getGpuTask());
        return true;
    }
    
    private boolean executeRunnableEvent (final SimEvent evt){
    	
        if(evt.getData() instanceof Runnable runnable) {
            runnable.run();
            return true;
        }

        throw new InvalidEventDataTypeException(evt, "CLOUDLET_UPDATE_ATTRIBUTES", "Runnable");
    }
    
    private boolean processGpuCloudletReady (final SimEvent evt) {
        final GpuCloudlet cloudlet = (GpuCloudlet)evt.getData();
        
        if(cloudlet.getStatus() == Cloudlet.Status.PAUSED)
             logGpuCloudletStatusChange (cloudlet, "resume execution of");
        else if (cloudlet.getGpuTask().getStatus() == GpuTask.Status.PAUSED)
        	logGpuCloudletStatusChange (cloudlet, "resume execution of GpuTask");
        else logGpuCloudletStatusChange (cloudlet, "start executing");
        
        cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler().gpuTaskReady(cloudlet.getGpuTask());
        cloudlet.getVm().getCloudletScheduler().cloudletReady(cloudlet);
        return true;
    }
    
    private void logGpuCloudletStatusChange (final GpuCloudlet cloudlet, final String status) {
        final String msg = cloudlet.getJobId() > 0 ? String.format("(job %d) ", cloudlet.getJobId()) : 
        	"";
        LOGGER.info("{}: {}: Request to {} {} {}received.", getSimulation().clockStr(), getName(), 
        		status, cloudlet, msg);
    }
    
    private boolean processGpuCloudletReturn (final SimEvent evt) {
        final GpuCloudlet cloudlet = (GpuCloudlet) evt.getData();
        
        gpucloudletsFinishedList.add(cloudlet);
        ((GpuVmSimple) cloudlet.getVm()).addExpectedFreePesNumber(cloudlet.getNumberOfPes());
        ((CustomVGpuSimple) cloudlet.getGpuTask().getVGpu()).addExpectedFreeCoresNumber(
        		cloudlet.getGpuTask().getNumberOfCores());
        
        String cloudletLifeTime = cloudlet.getLifeTime() == -1 ? "" : 
        	" (after defined cloudletLifetime expired)";
        String gpuTaskLifeTime = cloudlet.getGpuTask().getLifeTime() == -1 ? "" :
        	" (after defined gpuTaskLifetime expired)";
        String lifeTime = cloudletLifeTime + gpuTaskLifeTime; 
        LOGGER.info(
            "{}: {}: {} finished{} in {} and returned to broker.",
            getSimulation().clockStr(), getName(), cloudlet, lifeTime, cloudlet.getVm());

        if (cloudlet.getVm().getCloudletScheduler().isEmpty() &&
        		cloudlet.getGpuTask().getVGpu().getGpuTaskScheduler().isEmpty()) {
            //requestIdleVGpuDestruction(cloudlet.getGpuTask().getVGpu());
            requestIdleVmDestruction(cloudlet.getVm());
            return true;
        }

        //requestVGpuDestructionAfterGpuTaskFinished();
        requestVmDestructionAfterAllCloudletsFinished();
        return true;
    }
    
    private void requestVmDestructionAfterAllCloudletsFinished() {
        for (int i = gpuvmExecList.size() - 1; i >= 0; i--) 
            requestIdleVmDestruction(gpuvmExecList.get(i));
            
        if (gpucloudletWaitingList.isEmpty()) 
            return;
        
        requestDatacenterToCreateWaitingGpuVms(false, false);
    }
    
    /*private void requestVGpuDestructionAfterGpuTaskFinished () {
        for (int i = gpuvmExecList.size() - 1; i >= 0; i--) 
            requestIdleVGpuDestruction(gpuvmExecList.get(i).getVGpu());
    }*/
    
    /*@Override
    public DatacenterBroker requestIdleVGpuDestruction (CustomVGpu vgpu) {
    	if (vgpu.isCreated()) {
            if(isVGpuIdleEnough(vgpu) || isFinisheddd()) {
                LOGGER.info("{}: {}: Requesting {} destruction.", getSimulation().clockStr(), 
                		getName(), vgpu);
                sendNow(getDatacenter(vgpu.getGpuVm()), GpuCloudsimTags.VGPU_DESTROY, vgpu);
            }

            if(isVGpuIdlenessVerificationRequired((CustomVGpuSimple)vgpu)) {
                getSimulation().send(
                    new CloudSimEvent(vgpuDestructionDelayFunction.apply(vgpu),
                        vgpu.getGpuVm().getHost().getDatacenter(),
                        GpuCloudsimTags.VGPU_UPDATE_GPUTASK_PROCESSING));
                return this;
            }
        }
    }*/
    
    private boolean isVGpuIdlenessVerificationRequired (final CustomVGpuSimple vgpu) {
        if(vgpu.hasStartedSomeGpuTask() && vgpu.getGpuTaskScheduler().isEmpty()){
            final int schedulingInterval = (int)vgpu.getGpuVm().getHost().getDatacenter().
            		getSchedulingInterval();
            final int delay = vgpuDestructionDelayFunction.apply(vgpu).intValue();
            return delay > DEF_VGPU_DESTRUCTION_DELAY && 
            		(schedulingInterval <= 0 || delay % schedulingInterval != 0);
        }
        return false;
    }
    
    /*private boolean isVGpuIdleEnough (final CustomVGpu vgpu) {
        final double delay = vgpuDestructionDelayFunction.apply(vgpu);
        return delay > DEF_VGPU_DESTRUCTION_DELAY && vgpu.isIdleEnough(delay);
    }*/
    
    @Override
    public DatacenterBroker requestIdleVmDestruction (final Vm vm) {
        if (vm.isCreated()) {
            if(isGpuVmIdleEnough((GpuVm)vm) || isFinished()) {
                LOGGER.info("{}: {}: Requesting {} destruction.", getSimulation().clockStr(), 
                		getName(), vm);
                sendNow(getDatacenter(vm), CloudSimTag.VM_DESTROY, vm);
            }

            if(isVmIdlenessVerificationRequired((GpuVmSimple)vm) ||
            		isVGpuIdlenessVerificationRequired((CustomVGpuSimple)((GpuVmSimple)vm).getVGpu())) {
                getSimulation().send(
                    new CloudSimEvent(gpuvmDestructionDelayFunction.apply((GpuVm)vm),
                        vm.getHost().getDatacenter(),
                        CloudSimTag.VM_UPDATE_CLOUDLET_PROCESSING));
                return this;
            }
        }
        requestShutdownWhenIdle();
        return this;
    }
    
    @Override
    public void requestShutdownWhenIdle() {
        if (!shutdownRequested && isTimeToShutdownBroker()) {
            schedule(CloudSimTag.ENTITY_SHUTDOWN);
            shutdownRequested = true;
        }
    }
    
    private boolean isTimeToShutdownBroker() {
        return isAlive() && isTimeToTerminateSimulation() && shutdownWhenIdle && isBrokerIdle();
    }

    private boolean isTimeToTerminateSimulation() {
        return !getSimulation().isTerminationTimeSet() || getSimulation().isTimeToTerminateSimulationUnderRequest();
    }

    private boolean isBrokerIdle() {
        return gpucloudletWaitingList.isEmpty() && gpuvmWaitingList.isEmpty() && gpuvmExecList.isEmpty();
    }
    
    private boolean isGpuVmIdleEnough (final GpuVm vm) {
        final double delay = gpuvmDestructionDelayFunction.apply(vm);
        return delay > DEF_VM_DESTRUCTION_DELAY && vm.isIdleEnough(delay);
    }
    
    

    private boolean isVmIdlenessVerificationRequired (final GpuVmSimple vm) {
        if(vm.hasStartedSomeCloudlet() && vm.getCloudletScheduler().isEmpty()){
            final int schedulingInterval = (int)vm.getHost().getDatacenter().getSchedulingInterval();
            final int delay = gpuvmDestructionDelayFunction.apply(vm).intValue();
            return delay > DEF_VM_DESTRUCTION_DELAY && 
            		(schedulingInterval <= 0 || delay % schedulingInterval != 0);
        }
        return false;
    }

    protected Datacenter getDatacenter (final Vm vm) {
        return vm.getHost().getDatacenter();
    }
    
    private boolean requestDatacenterToCreateWaitingGpuVms (final boolean isFallbackDatacenter, 
    		final boolean creationRetry) {
        for (final GpuVm vm : gpuvmWaitingList) {
            this.lastSelectedGpuDc = isFallbackDatacenter && selectClosestGpuDatacenter ?
                                        defaultGpuDatacenterMapper(lastSelectedGpuDc, vm) :
                                        gpudatacenterMapper.apply(lastSelectedGpuDc, vm);
            if(creationRetry) {
                vm.setLastTriedDatacenter(GpuDatacenter.NULL);
            }
            this.gpuvmCreationRequests += requestGpuVmCreation(lastSelectedGpuDc, 
            		isFallbackDatacenter, vm);
        }

        return lastSelectedGpuDc != GpuDatacenter.NULL;
    }
    
    private int requestGpuVmCreation(final GpuDatacenter datacenter, final boolean isFallbackDatacenter, 
    		final GpuVm vm) {
        if (datacenter == GpuDatacenter.NULL || datacenter.equals(vm.getLastTriedDatacenter())) {
            return 0;
        }

        logGpuVmCreationRequest(datacenter, isFallbackDatacenter, vm);
        send(datacenter, vm.getSubmissionDelay(), CloudSimTag.VM_CREATE_ACK, vm);
        vm.setLastTriedDatacenter(datacenter);
        return 1;
    }
    
    private void logGpuVmCreationRequest (final GpuDatacenter datacenter, 
    		final boolean isFallbackDatacenter, final GpuVm vm) {
        final String fallbackMsg = isFallbackDatacenter ? " (due to lack of a suitable Host in previous "
        		+ "one)" : "";
        if(vm.getSubmissionDelay() == 0)
            LOGGER.info(
                "{}: {}: Trying to create {} in {}{}",
                getSimulation().clockStr(), getName(), vm, datacenter.getName(), fallbackMsg);
        else
            LOGGER.info(
                "{}: {}: Creation of {} in {}{} will be requested in {} seconds",
                getSimulation().clockStr(), getName(), vm, datacenter.getName(),
                fallbackMsg, vm.getSubmissionDelay());
    }

    protected abstract GpuDatacenter defaultGpuDatacenterMapper (GpuDatacenter lastDatacenter, GpuVm vm);
}
