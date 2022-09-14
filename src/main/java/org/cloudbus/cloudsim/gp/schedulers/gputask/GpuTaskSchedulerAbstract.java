package org.cloudbus.cloudsim.gp.schedulers.gputask;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTaskExecution;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.vgpu.VGpuSimple;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

import org.cloudsimplus.listeners.EventListener;
import org.gpucloudsimplus.listeners.GpuTaskResourceAllocationFailEventInfo;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.util.Conversion;
import org.cloudbus.cloudsim.resources.Ram;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.collectingAndThen;
import static org.gpucloudsimplus.listeners.GpuTaskResourceAllocationFailEventInfo.of;

import java.util.function.BiPredicate;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.*;


public abstract class GpuTaskSchedulerAbstract implements GpuTaskScheduler {
	
	private final List<GpuTaskExecution> gpuTaskPausedList;
    private final List<GpuTaskExecution> gpuTaskFinishedList;
    private final List<GpuTaskExecution> gpuTaskFailedList;
    private final List<GpuTaskExecution> gpuTaskExecList;
    private final List<GpuTaskExecution> gpuTaskWaitingList;
    
    private final List<GpuTask> gpuTaskSubmittedList;
    private final Set<GpuTask> gpuTaskReturnedList;
    
    //private CloudletTaskScheduler taskScheduler;
    private double previousTime;
    private MipsShare currentMipsShare;
    private boolean enableGpuTaskSubmittedList;
    
    private VGpu vgpu;
    
    private final List<EventListener<GpuTaskResourceAllocationFailEventInfo>> resourceAllocationFailListeners;

    protected GpuTaskSchedulerAbstract () {
        setPreviousTime(0.0);
        vgpu = VGpu.NULL;
        gpuTaskSubmittedList = new ArrayList<>();
        gpuTaskExecList = new ArrayList<>();
        gpuTaskPausedList = new ArrayList<>();
        gpuTaskFinishedList = new ArrayList<>();
        gpuTaskFailedList = new ArrayList<>();
        gpuTaskWaitingList = new ArrayList<>();
        gpuTaskReturnedList = new HashSet<>();
        currentMipsShare = new MipsShare();
        //taskScheduler = CloudletTaskScheduler.NULL;
        resourceAllocationFailListeners = new ArrayList<>();
    }
    
    protected final void setPreviousTime (final double previousTime) {
        this.previousTime = previousTime;
    }
    
    @Override
    public double getPreviousTime () {
        return previousTime;
    }

    protected void setCurrentMipsShare (final MipsShare currentMipsShare) {
        if(currentMipsShare.pes() > vgpu.getNumberOfCores()){
            LOGGER.warn ("Requested {} PEs but {} has just {}", currentMipsShare.pes(), vgpu, 
            		vgpu.getNumberOfCores());
            this.currentMipsShare = new MipsShare (vgpu.getNumberOfCores(), currentMipsShare.mips());
        }
        else this.currentMipsShare = currentMipsShare;
    }
    
    public MipsShare getCurrentMipsShare () {
        return currentMipsShare;
    }

    public double getAvailableMipsByCore () {
        final long totalCoresOfAllExecGpuTask = totalCoresOfAllExecGpuTask();
        if(totalCoresOfAllExecGpuTask > currentMipsShare.pes()) {
            return getTotalMipsShare() / totalCoresOfAllExecGpuTask;
        }

        return getCoreCapacity ();
    }

    private Double getCoreCapacity () {
        return currentMipsShare.mips();
    }
    
    private long totalCoresOfAllExecGpuTask () {
        return gpuTaskExecList.stream()
            .map(GpuTaskExecution::getGpuTask)
            .mapToLong(GpuTask::getNumberOfCores).sum();
    }

    private double getTotalMipsShare (){
        return currentMipsShare.totalMips();
    }
    
    @Override
    public List<GpuTaskExecution> getGpuTaskExecList () {
        return Collections.unmodifiableList(gpuTaskExecList);
    }

    @Override
    public <T extends GpuTask> List<T> getGpuTaskSubmittedList () {
        if(gpuTaskSubmittedList.isEmpty() && !enableGpuTaskSubmittedList) {
            LOGGER.warn("{}: The list of submitted GpuTask for {} is empty maybe because you didn't enabled it by calling enableGpuTaskSubmittedList().", getClass().getSimpleName(), vgpu);
        }

        return (List<T>) gpuTaskSubmittedList;
    }

    @Override
    public GpuTaskScheduler enableCloudletSubmittedList () {
        this.enableGpuTaskSubmittedList = true;
        return this;
    }
    
    protected void addGpuTaskToWaitingList (final GpuTaskExecution gte) {
        if(requireNonNull(gte) == GpuTaskExecution.NULL){
            return;
        }

        if(gte.getGpuTask().getStatus() != GpuTask.Status.FROZEN) {
            gte.setStatus(GpuTask.Status.QUEUED);
        }
        gpuTaskWaitingList.add(gte);
    }

    protected List<GpuTaskExecution> getGpuTaskPausedList () {
        return gpuTaskPausedList;
    }
    
    @Override
    public List<GpuTaskExecution> getGpuTaskFinishedList () {
        return gpuTaskFinishedList;
    }

    protected List<GpuTaskExecution> getGpuTaskFailedList () {
        return gpuTaskFailedList;
    }
    
    @Override
    public List<GpuTaskExecution> getGpuTaskWaitingList () {
        return Collections.unmodifiableList(gpuTaskWaitingList);
    }

    protected void sortGpuTaskWaitingList (final Comparator<GpuTaskExecution> comparator) {
    	gpuTaskWaitingList.sort(comparator);
    }

    @Override
    public final double gpuTaskSubmit (final GpuTask gpuTask) {
        return gpuTaskSubmit(gpuTask, 0.0);
    }
    
    @Override
    public final double gpuTaskSubmit (final GpuTask gpuTask, final double fileTransferTime) {
        if(enableGpuTaskSubmittedList) {
        	gpuTaskSubmittedList.add(gpuTask);
        }

        return gpuTaskSubmitInternal(new GpuTaskExecution(gpuTask), fileTransferTime);
    }
    
    protected double gpuTaskSubmitInternal (final GpuTaskExecution gte, 
    		final double fileTransferTime) {
        if (canExecuteGpuTask(gte)) {
            gte.setStatus(GpuTask.Status.INEXEC);
            gte.setFileTransferTime(fileTransferTime);
            addGpuTaskToExecList(gte);
            return fileTransferTime + Math.abs(gte.getGpuTaskLength()/getCoreCapacity()) ;
        }

        // No enough free PEs, then add Cloudlet to the waiting queue
        addGpuTaskToWaitingList(gte);
        return 0.0;
    }
    
    protected void addGpuTaskToExecList (final GpuTaskExecution gte) {
        gte.setStatus(GpuTask.Status.INEXEC);
        gte.setLastProcessingTime(getVGpu().getSimulation().clock());
        gpuTaskExecList.add(gte);
        addUsedCores(gte.getNumberOfCores());
    }

    @Override
    public boolean hasFinishedGpuTasks () {
        return !gpuTaskFinishedList.isEmpty();
    }
    
    protected Optional<GpuTaskExecution> findGpuTaskInAllLists (final double gpuTaskId) {
        final Stream<List<GpuTaskExecution>> gpuTaskExecInfoListStream = Stream.of (
        		gpuTaskExecList, gpuTaskPausedList, gpuTaskWaitingList, gpuTaskFinishedList, 
        		gpuTaskFailedList);

        return gpuTaskExecInfoListStream
            .flatMap(List::stream)
            .filter(gte -> gte.getGpuTaskId() == gpuTaskId)
            .findFirst();
    }
    
    protected Optional<GpuTaskExecution> findGpuTaskInList (final GpuTask gpuTask, 
    		final List<GpuTaskExecution> list) {
    	
        return list.stream()
            .filter(gte -> gte.getGpuTaskId() == gpuTask.getTaskId())
            .findFirst();
    }
    
    protected void gpuTaskFinish (final GpuTaskExecution gte) {
        gte.setStatus(GpuTask.Status.SUCCESS);
        gte.finalizeCloudlet();
        gpuTaskFinishedList.add(gte);
    }
    
    @Override
    public boolean gpuTaskReady (final GpuTask gpuTask) {
        if (changeStatusOfGpuTaskIntoList(gpuTaskPausedList, gpuTask, this::changeToReady)) {
            return true;
        }

        gpuTask.setStatus(GpuTask.Status.READY);

 
        //final Datacenter dc = vm.getHost().getDatacenter();
        //dc.schedule(CloudSimTag.VM_UPDATE_CLOUDLET_PROCESSING);
        return true;
    }
    
    private void changeToReady (final GpuTaskExecution gte) {
        changeStatusOfGpuTask(gte, gte.getGpuTask().getStatus(), GpuTask.Status.READY);
    }

    @Override
    public boolean gpuTaskPause (final GpuTask gpuTask) {
        if (changeStatusOfGpuTaskIntoList(gpuTaskExecList, gpuTask, this::changeInExecToPaused)) {
            return true;
        }

        return changeStatusOfGpuTaskIntoList(gpuTaskWaitingList, gpuTask, 
        		this::changeReadyToPaused);
    }

    private void changeInExecToPaused (final GpuTaskExecution gte) {
        changeStatusOfGpuTask(gte, GpuTask.Status.INEXEC, GpuTask.Status.PAUSED);
        removeUsedCores(gte.getNumberOfCores());
    }
    
    private void changeReadyToPaused(final GpuTaskExecution gte) {
        changeStatusOfGpuTask(gte, GpuTask.Status.READY, GpuTask.Status.PAUSED);
    }

    @Override
    public GpuTask gpuTaskFail (final GpuTask gpuTask) {
        return stopGpuTask(gpuTask, GpuTask.Status.FAILED);
    }

    @Override
    public GpuTask gpuTaskCancel (final GpuTask gpuTask) {
        return stopGpuTask(gpuTask, GpuTask.Status.CANCELED);
    }
    
    private GpuTask stopGpuTask (final GpuTask gpuTask, final GpuTask.Status stopStatus) {
    	
        boolean found = changeStatusOfGpuTaskIntoList(gpuTaskFinishedList, gpuTask, gte -> {});
        if (found) {
            return gpuTask;
        }

        found = changeStatusOfGpuTaskIntoList (
        		gpuTaskExecList, gpuTask,
        		gte -> changeStatusOfGpuTask (gte, GpuTask.Status.INEXEC, stopStatus));
        if (found) {
            return gpuTask;
        }

        found = changeStatusOfGpuTaskIntoList (
        		gpuTaskPausedList, gpuTask,
        		gte -> changeStatusOfGpuTask(gte, GpuTask.Status.PAUSED, stopStatus));
        if (found) {
            return gpuTask;
        }

        changeStatusOfGpuTaskIntoList (
        		gpuTaskWaitingList, gpuTask,
            gte -> changeStatusOfGpuTask(gte, GpuTask.Status.READY, stopStatus));
        if (found) {
            return gpuTask;
        }

        return gpuTask.NULL;
    }

    private void changeStatusOfGpuTask (final GpuTaskExecution gte, 
    		final GpuTask.Status currentStatus, final GpuTask.Status newStatus) {
    	
        if ((currentStatus == GpuTask.Status.INEXEC || currentStatus == GpuTask.Status.READY) 
        		&& gte.getGpuTask().isFinished())
        	gpuTaskFinish(gte);
        else gte.setStatus(newStatus);

        if (newStatus == GpuTask.Status.PAUSED)
        	gpuTaskPausedList.add(gte);
        else if (newStatus == GpuTask.Status.READY)
            addGpuTaskToWaitingList(gte);
    }
    
    private boolean changeStatusOfGpuTaskIntoList (
            final List<GpuTaskExecution> gpuTaskList,
            final GpuTask gpuTask,
            final Consumer<GpuTaskExecution> gpuTaskStatusUpdaterConsumer) {
    	
            final Function<GpuTaskExecution, GpuTask> removeGpuTaskAndUpdateStatus = gte -> {
            	gpuTaskList.remove(gte);
            	gpuTaskStatusUpdaterConsumer.accept(gte);
                return gte.getGpuTask();
            };

            return findGpuTaskInList(gpuTask, gpuTaskList)
                .map(removeGpuTaskAndUpdateStatus)
                .isPresent();
    }
    
    @Override
    public double updateProcessing (final double currentTime, final MipsShare mipsShare) {
        setCurrentMipsShare(mipsShare);

        if (isEmpty()) {
            setPreviousTime(currentTime);
            return Double.MAX_VALUE;
        }

        deallocateVGpuResources();

        double nextSimulationDelay = updateGpuTasksProcessing(currentTime);
        nextSimulationDelay = Math.min(nextSimulationDelay, 
        		moveNextGpuTasksFromWaitingToExecList (currentTime));
        addGpuTasksToFinishedList();

        setPreviousTime(currentTime);
        vgpu.getSimulation().setLastCloudletProcessingUpdate(currentTime);

        return nextSimulationDelay;
    }
    
    private void deallocateVGpuResources () {
        ((VGpuSimple)vgpu).getGddram().deallocateAllResources();
        ((VGpuSimple)vgpu).getBw().deallocateAllResources();
    }
    
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private double updateGpuTasksProcessing (final double currentTime) {
        double nextGpuTaskFinishTime = Double.MAX_VALUE;
        long usedCores= 0;
        

        for (int i = 0; i < gpuTaskExecList.size(); i++) {
            final GpuTaskExecution gte = gpuTaskExecList.get(i);
            updateGpuTaskProcessingAndPacketsDispatch (gte, currentTime);
            nextGpuTaskFinishTime = Math.min(nextGpuTaskFinishTime, 
            		gpuTaskEstimatedFinishTime(gte, currentTime));
            usedCores += gte.getGpuTask().getNumberOfCores();
        }

        ((VGpuSimple) vgpu).setFreeCoresNumber(vgpu.getNumberOfCores() - usedCores);

        return nextGpuTaskFinishTime;
    }
    
    private void updateGpuTaskProcessingAndPacketsDispatch (final GpuTaskExecution gte, 
    		final double currentTime) {
        long partialFinishedMI = 0;
        /*if (taskScheduler.isTimeToUpdateCloudletProcessing(gte.getGpuTask())) {
            partialFinishedMI = updateGpuTaskProcessing(gte, currentTime);
        }*/
        partialFinishedMI = updateGpuTaskProcessing(gte, currentTime);

        //taskScheduler.processCloudletTasks(gte.getGpuTask(), partialFinishedMI);
    }

    protected long updateGpuTaskProcessing (final GpuTaskExecution gte, final double currentTime) {
        final double partialFinishedInstructions = gpuTaskExecutedInstructionsForTimeSpan (gte, 
        		currentTime);
        gte.updateProcessing(partialFinishedInstructions);
        updateVGpuResourceAbsoluteUtilization(gte, ((VGpuSimple)vgpu).getGddram());
        updateVGpuResourceAbsoluteUtilization(gte, ((VGpuSimple)vgpu).getBw());

        return (long)(partialFinishedInstructions/ Conversion.MILLION);
    }
    
    private void updateVGpuResourceAbsoluteUtilization (final GpuTaskExecution gte, 
    		final ResourceManageable vgpuResource) {
    	
        final GpuTask gpuTask = gte.getGpuTask();
        final long requested = (long) getGpuTaskResourceAbsoluteUtilization(gpuTask, vgpuResource);
        if(requested > vgpuResource.getCapacity()){
            LOGGER.warn(
                "{}: {}: {} requested {} {} of {} but that is >= the VGPU capacity ({})",
                vgpu.getSimulation().clockStr(), getClass().getSimpleName(), gpuTask,
                requested, vgpuResource.getUnit(), vgpuResource.getClass().getSimpleName(), 
                vgpuResource.getCapacity());
            return;
        }

        final long available = vgpuResource.getAvailableResource();
        if(requested > available){
            final String msg1 =
                    available > 0 ?
                    String.format("just %d was available", available):
                    "no amount is available.";
            final String msg2 = vgpuResource.getClass() == Ram.class ? 
            		". Using Virtual Memory," : ",";
            LOGGER.warn(
                "{}: {}: {} requested {} {} of {} but {}{} which delays Cloudlet processing.",
                vgpu.getSimulation().clockStr(), getClass().getSimpleName(), gpuTask,
                requested, vgpuResource.getUnit(), vgpuResource.getClass().getSimpleName(), 
                msg1, msg2);

            updateOnResourceAllocationFailListeners(vgpuResource, gpuTask, 
            		requested, available);
        }

        vgpuResource.allocateResource(Math.min(requested, available));
    }
    
    private void updateOnResourceAllocationFailListeners (
            final ResourceManageable resource, final GpuTask gpuTask,
            final long requested, final long available) {
    	
    	for (int i = resourceAllocationFailListeners.size()-1; i >= 0; i--) {
    		final EventListener<GpuTaskResourceAllocationFailEventInfo> listener = 
    				resourceAllocationFailListeners.get(i);
    		listener.update(of(listener, gpuTask, resource.getClass(), requested, available, 
    				vgpu.getSimulation().clock()));
        }
    }
    
	@Override
    public GpuTaskScheduler addOnGpuTaskResourceAllocationFail (
    		final EventListener<GpuTaskResourceAllocationFailEventInfo> listener) {
        if(EventListener.NULL.equals(listener)){
            return this;
        }

        resourceAllocationFailListeners.add(requireNonNull(listener));
        return this;
    }

    @Override
    public boolean removeOnGpuTaskResourceAllocationFail (
    		final EventListener<GpuTaskResourceAllocationFailEventInfo> listener) {
        return resourceAllocationFailListeners.remove(listener);
    }
    
    private double getGpuTaskResourceAbsoluteUtilization (final GpuTask gpuTask,
            final ResourceManageable vgpuResource) {
    	final UtilizationModel um = gpuTask.getUtilizationModel(vgpuResource.getClass());
        return um.getUnit() == UtilizationModel.Unit.ABSOLUTE ?
        	Math.min(um.getUtilization(), vgpuResource.getCapacity()) :
         	um.getUtilization() * vgpuResource.getCapacity();
	}
    
    private double gpuTaskExecutedInstructionsForTimeSpan (final GpuTaskExecution gte, 
    		final double currentTime) {
        
        final double processingTimeSpan = hasGpuTaskFileTransferTimePassed(gte, 
        		currentTime) ? timeSpan(gte, currentTime) : 0;

        //final double vMemDelay = getVirtualMemoryDelay(gte, processingTimeSpan);
        final double reducedBwDelay = getBandwidthOverSubscriptionDelay(gte, processingTimeSpan);
        
        //vMemDelay == Double.MIN_VALUE &&
        if( reducedBwDelay == Double.MIN_VALUE) {
            return 0;
        }

        final double gpuTaskUsedMips = getAllocatedMipsForGpuTask(gte, currentTime, true);
        //validateDelay(vMemDelay) +
        final double actualProcessingTime = processingTimeSpan - (validateDelay(reducedBwDelay));
        return gpuTaskUsedMips * actualProcessingTime * Conversion.MILLION;
    }

    private double validateDelay (final double delay) {
        return delay == Double.MIN_VALUE ? 0 : delay;
    }
    
    /*private double getVirtualMemoryDelay (final GpuTaskExecution gte, 
    		final double processingTimeSpan) {
    	gg;
        return getResourceOverSubscriptionDelay(
            gte, processingTimeSpan, ((VGpuSimple)vgpu).getGddram(),

            (vgpuGddram, requestedRam) -> requestedRam <= vgpuGddram.getCapacity() && 
            requestedRam <= vm.getStorage().getAvailableResource(),

            (notAllocatedRam, __) -> diskTransferTime(gte, notAllocatedRam));
    }
    
    private double diskTransferTime (final GpuTaskExecution gte, final Double dataSize) {
        return gte.getGpuTask().getVm().getHost().getStorage().getTransferTime(dataSize.intValue());
    }*/

    private double getBandwidthOverSubscriptionDelay (final GpuTaskExecution gte, 
    		final double processingTimeSpan) {
        return getResourceOverSubscriptionDelay(
            gte, processingTimeSpan, ((VGpuSimple)vgpu).getBw(),
            (vgpuBw, requestedBw) -> requestedBw <= vgpuBw.getCapacity(),

            (notAllocatedBw, requestedBw) -> requestedBw/(requestedBw-notAllocatedBw) - 1);
    }
    
    private double getResourceOverSubscriptionDelay (final GpuTaskExecution gte, 
    		final double processingTimeSpan,
            final ResourceManageable vgpuResource,
            final BiPredicate<ResourceManageable, Double> suitableCapacityPredicate,
            final BiFunction<Double, Double, Double> delayFunction) {
    	
    	final double requestedResource = getGpuTaskResourceAbsoluteUtilization (
    			gte.getGpuTask(), vgpuResource);

    	if(!suitableCapacityPredicate.test(vgpuResource, requestedResource)) {
                gte.incOverSubscriptionDelay(processingTimeSpan);
                return Double.MIN_VALUE;
    	}

    	final double notAllocatedResource = Math.max(requestedResource - 
    			vgpuResource.getAvailableResource(), 0);
    	if (notAllocatedResource > 0) {
    		final double delay = delayFunction.apply(notAllocatedResource, requestedResource);
    		gte.incOverSubscriptionDelay(delay);
    		return delay;
    	}

    	return 0;
	}
    
    private boolean hasGpuTaskFileTransferTimePassed (final GpuTaskExecution gte, 
    		final double currentTime) {
        return gte.getFileTransferTime() == 0 ||
               currentTime - gte.getGpuTaskArrivalTime() > gte.getFileTransferTime() ||
               gte.getGpuTask().getFinishedLengthSoFar() > 0;
    }
    
    protected double timeSpan (final GpuTaskExecution gte, final double currentTime) {
        return currentTime - gte.getLastProcessingTime();
    }
    
    private int addGpuTasksToFinishedList () {
        final List<GpuTaskExecution> finishedGpuTasks
            = gpuTaskExecList.stream()
            .filter(gte -> gte.getGpuTask().isFinished())
            .collect(toList());

        for (final GpuTaskExecution gt : finishedGpuTasks) {
            addGpuTaskToFinishedList(gt);
        }

        return finishedGpuTasks.size();
    }

    private void addGpuTaskToFinishedList (final GpuTaskExecution gte) {
        setGpuTaskFinishTimeAndAddToFinishedList(gte);
        removeGpuTaskFromExecList(gte);
    }
    
    protected GpuTaskExecution removeGpuTaskFromExecList (final GpuTaskExecution gte) {
        removeUsedCores(gte.getNumberOfCores());
        return gpuTaskExecList.remove(gte) ? gte : GpuTaskExecution.NULL;
    }

    private void setGpuTaskFinishTimeAndAddToFinishedList (final GpuTaskExecution gte) {
        final double clock = vgpu.getSimulation().clock();
        gpuTaskFinish(gte);
        gte.setFinishTime(clock);
    }
    
    protected double gpuTaskEstimatedFinishTime (final GpuTaskExecution gte, 
    		final double currentTime) {
    	
        final double gpuTaskAllocatedMips = getAllocatedMipsForGpuTask(gte, currentTime);
        gte.setLastAllocatedMips(gpuTaskAllocatedMips);
        final double remainingLifeTime = gte.getRemainingLifeTime();

        final double finishTimeForRemainingLen = gte.getRemainingCloudletLength() / gte.getLastAllocatedMips();

        final double estimatedFinishTime = Math.min(remainingLifeTime, finishTimeForRemainingLen);
        return Math.max(estimatedFinishTime, vgpu.getSimulation().getMinTimeBetweenEvents());
    }
    
    protected double moveNextGpuTasksFromWaitingToExecList (final double currentTime) {
    	
        Optional<GpuTaskExecution> optional = Optional.of(GpuTaskExecution.NULL);
        double nextGpuTaskFinishTime = Double.MAX_VALUE;
        while (!gpuTaskWaitingList.isEmpty() && optional.isPresent()) {
            optional = findSuitableWaitingGpuTask();
            final double estimatedFinishTime =
                optional
                    .map(this::addWaitingGpuTaskToExecList)
                    .map(gte -> gpuTaskEstimatedFinishTime(gte, currentTime))
                    .orElse(Double.MAX_VALUE);
            nextGpuTaskFinishTime = Math.min(nextGpuTaskFinishTime, estimatedFinishTime);
        }

        return nextGpuTaskFinishTime;
    }
    
    protected Optional<GpuTaskExecution> findSuitableWaitingGpuTask () {
        return gpuTaskWaitingList
                .stream()
                .filter(cle -> cle.getGpuTask().getStatus() != GpuTask.Status.FROZEN)
                .filter(this::canExecuteGpuTask)
                .findFirst();
    }
    
    protected boolean isThereEnoughFreeCoresForGpuTask (final GpuTaskExecution gte) {
        return vgpu.getVGpuCore().getAvailableResource() >= gte.getNumberOfCores();
    }

    protected GpuTaskExecution addWaitingGpuTaskToExecList (final GpuTaskExecution gte) {
        
    	gpuTaskWaitingList.remove(gte);
        addGpuTaskToExecList(gte);
        return gte;
    }

    @Override
    public VGpu getVGpu () {
        return vgpu;
    }

    @Override
    public void setVGpu (final VGpu vgpu) {
        if (isOtherVGpuAssigned(requireNonNull(vgpu))) {
            throw new IllegalArgumentException(
                "GpuTaskScheduler already has a vgpu assigned to it. Each vgpu must have its "
                + "own GpuTaskScheduler instance.");
        }

        this.vgpu = vgpu;
    }
    
    private boolean isOtherVGpuAssigned (final VGpu vgpu) {
        return this.vgpu != null && this.vgpu != VGpu.NULL && !vgpu.equals(this.vgpu);
    }

    @Override
    public long getUsedCores() {
        return vgpu.getVGpuCore().getAllocatedResource();
    }

    @Override
    public long getFreeCores () {
        return currentMipsShare.pes() - getUsedCores();
    }
    
    private void addUsedCores (final long usedCoresToAdd) {
        vgpu.getVGpuCore().allocateResource(usedCoresToAdd);
    }

    private void removeUsedCores (final long usedCoresToRemove) {
        vgpu.getVGpuCore().deallocateResource(usedCoresToRemove);
    }

    ////////////////////////////////////////////////////////
    /*@Override
    public CloudletTaskScheduler getTaskScheduler() {
        return taskScheduler;
    }
    
    @Override
    public void setTaskScheduler(final CloudletTaskScheduler taskScheduler) {
        this.taskScheduler = requireNonNull(taskScheduler);
        this.taskScheduler.setVm(vm);
    }

    @Override
    public boolean isThereTaskScheduler() {
        return taskScheduler != null && taskScheduler != CloudletTaskScheduler.NULL;
    }*/
    ////////////////////////////////////////////////////////

    @Override
    public double getRequestedGpuPercent (final double time) {
        return getRequestedOrAllocatedGpuPercentUtilization(time, true);
    }

    @Override
    public double getAllocatedGpuPercent (final double time) {
        return getRequestedOrAllocatedGpuPercentUtilization(time, false);
    }

    private double getRequestedOrAllocatedGpuPercentUtilization(final double time, 
    		final boolean requestedUtilization) {
        return gpuTaskExecList.stream()
            .map(GpuTaskExecution::getGpuTask)
            .mapToDouble(gpuTask -> getAbsoluteGpuTaskGpuUtilizationForAllCores(time, gpuTask, 
            		requestedUtilization))
            .sum() / vgpu.getTotalMipsCapacity();
    }
    
    private double getAbsoluteGpuTaskGpuUtilizationForAllCores (final double time, 
    		final GpuTask gpuTask, final boolean requestedUtilization) {
        final double gpuTaskGpuUsageForOneCore =
            getAbsoluteGpuTaskResourceUtilization(
            		gpuTask, gpuTask.getUtilizationModelGpu(), time, getAvailableMipsByCore(), "GPU", requestedUtilization);

        return gpuTaskGpuUsageForOneCore * gpuTask.getNumberOfCores();
    }
    
    protected double getRequestedMipsForGpuTask (final GpuTaskExecution gte, final double time) {
        final GpuTask gpuTask = gte.getGpuTask();
        return getAbsoluteGpuTaskResourceUtilization(gpuTask, gpuTask.getUtilizationModelGpu(), 
        		time, vgpu.getMips(), "GPU", true);
    }

    public double getAllocatedMipsForGpuTask (final GpuTaskExecution gte, final double time) {
        return getAllocatedMipsForGpuTask(gte, time, false);
    }
    
    public double getAllocatedMipsForGpuTask (final GpuTaskExecution gte, final double time, 
    		final boolean log) {
        final GpuTask gpuTask = gte.getGpuTask();
        final String resourceName = log ? "GPU" : "";
        return getAbsoluteGpuTaskResourceUtilization(gpuTask, gpuTask.getUtilizationModelGpu(), 
        		time, getAvailableMipsByCore(), resourceName, false);
    }
    
    @Override
    public double getCurrentRequestedBwPercentUtilization () {
        return gpuTaskExecList.stream()
            .map(GpuTaskExecution::getGpuTask)
            .mapToDouble(gt -> getAbsoluteGpuTaskResourceUtilization(gt, gt.getUtilizationModelBw(), 
            		vgpu.getBw().getCapacity(), "BW"))
            .sum() / vgpu.getBw().getCapacity();
    }

    @Override
    public double getCurrentRequestedGddramPercentUtilization () {
        return gpuTaskExecList.stream()
            .map(GpuTaskExecution::getGpuTask)
            .mapToDouble(gt -> getAbsoluteGpuTaskResourceUtilization(gt, 
            		gt.getUtilizationModelGddram(), vgpu.getGddram().getCapacity(), "RAM"))
            .sum() / vgpu.getGddram().getCapacity();
    }
    
    private double getAbsoluteGpuTaskResourceUtilization (final GpuTask gpuTask, 
    		final UtilizationModel model, final double maxResourceAllowedToUse, 
    		final String resource) {
    	return getAbsoluteGpuTaskResourceUtilization(gpuTask, model, 
    			vgpu.getSimulation().clock(), maxResourceAllowedToUse, resource, true);
		}
    
    private double getAbsoluteGpuTaskResourceUtilization (
            final GpuTask gpuTask,
            final UtilizationModel model,
            final double time,
            final double maxResourceAllowedToUse,
            final String resourceName,
            final boolean requestedUtilization) {
    	
    	if (model.getUnit() == UtilizationModel.Unit.ABSOLUTE) {
        	return Math.min(model.getUtilization(time), maxResourceAllowedToUse);
        }

    	final double requestedPercent = model.getUtilization();
    	final double allocatedPercent = requestedUtilization ? requestedPercent : 
    		Math.min(requestedPercent, 1);

        if(requestedPercent > 1 && !requestedUtilization && !resourceName.isEmpty()) {
        	LOGGER.warn(
        			"{}: {}: {} is requesting {}% of the total {} capacity which cannot be allocated. Allocating {}%.",
                    vgpu.getSimulation().clockStr(), getClass().getSimpleName(), gpuTask,
                    requestedPercent*100, resourceName, allocatedPercent*100);
        }
        return allocatedPercent * maxResourceAllowedToUse;
	}
    
    protected Set<GpuTask> getGpuTaskReturnedList () {
        return Collections.unmodifiableSet(gpuTaskReturnedList);
    }

    @Override
    public void addGpuTaskToReturnedList (final GpuTask gpuTask) {
        this.gpuTaskReturnedList.add(gpuTask);
    }

    @Override
    public void deallocateCoresFromVGpu (final long coresToRemove) {
        final long removedCores = currentMipsShare.remove(coresToRemove);
        removeUsedCores(removedCores);
    }

    @Override
    public List<GpuTask> getGpuTaskList () {
        return Stream.concat(gpuTaskExecList.stream(), gpuTaskWaitingList.stream())
                     .map(GpuTaskExecution::getGpuTask)
                     .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Override
    public boolean isEmpty() {
        return gpuTaskExecList.isEmpty() && gpuTaskWaitingList.isEmpty();
    }
    
    private boolean canExecuteGpuTask (final GpuTaskExecution gte) {
        return gte.getGpuTask().getStatus().ordinal() < GpuTask.Status.FROZEN.ordinal() && 
        		canExecuteGpuTaskInternal(gte);
    }

    protected abstract boolean canExecuteGpuTaskInternal(GpuTaskExecution gte);

    @Override
    public void clear() {
        this.gpuTaskWaitingList.clear();
        this.gpuTaskExecList.clear();
    }

}
