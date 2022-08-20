package org.cloudbus.cloudsim.gp.schedulers.gputask;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTaskExecution;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.resources.CustomVGpuSimple;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

import org.cloudsimplus.listeners.EventListener;
import org.gpucloudsimplus.listeners.GpuTaskResourceAllocationFailEventInfo;

import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.util.Conversion;
import org.cloudbus.cloudsim.resources.Ram;

import static java.util.Objects.requireNonNull;
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
    private boolean enableGpuTastSubmittedList;
    
    private CustomVGpu vgpu;
    
    private final List<EventListener<GpuTaskResourceAllocationFailEventInfo>> resourceAllocationFailListeners;

    protected GpuTaskSchedulerAbstract () {
        setPreviousTime(0.0);
        vgpu = CustomVGpu.NULL;
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
        if(currentMipsShare.pes() > vgpu.getNumberOfPes()){
            LOGGER.warn ("Requested {} PEs but {} has just {}", currentMipsShare.pes(), vgpu, 
            		vgpu.getNumberOfPes());
            this.currentMipsShare = new MipsShare (vgpu.getNumberOfPes(), currentMipsShare.mips());
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
            .mapToLong(GpuTask::getNumberOfPes).sum();
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
    
    protected Optional<GpuTaskExecution> findCloudletInList(final GpuTask gpuTask, 
    		final List<GpuTaskExecution> list) {
    	
        return list.stream()
            .filter(gte -> gte.getGpuTaskId() == gpuTask.getId())
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

        /*
         Requests a gpuTask processing update to ensure the gpuTask will be moved to the
         exec list as soon as possible.
         Without such a request, the GpuTask may start executing only
         after a new and possibly unrelated message is processed by the simulator.
         Since the next message to be received may take a long time,
         the processing update is requested right away.
         */
        final Datacenter dc = vm.getHost().getDatacenter();
        dc.schedule(CloudSimTag.VM_UPDATE_CLOUDLET_PROCESSING);
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

            return findCloudletInList(gpuTask, gpuTaskList)
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
        ((CustomVGpuSimple)vgpu).getGddram().deallocateAllResources();
        ((CustomVGpuSimple)vgpu).getBw().deallocateAllResources();
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
            usedCores += gte.getGpuTask().getNumberOfPes();
        }

        ((CustomVGpuSimple) vgpu).setFreePesNumber(vgpu.getNumberOfPes() - usedCores);

        return nextGpuTaskFinishTime;
    }
    
    private void updateGpuTaskProcessingAndPacketsDispatch (final GpuTaskExecution gte, 
    		final double currentTime) {
        long partialFinishedMI = 0;
        if (taskScheduler.isTimeToUpdateCloudletProcessing(gte.getCloudlet())) {
            partialFinishedMI = updateGpuTaskProcessing(gte, currentTime);
        }

        taskScheduler.processCloudletTasks(gte.getGpuTask(), partialFinishedMI);
    }

    protected long updateGpuTaskProcessing (final GpuTaskExecution gte, final double currentTime) {
        final double partialFinishedInstructions = gpuTaskExecutedInstructionsForTimeSpan (gte, 
        		currentTime);
        gte.updateProcessing(partialFinishedInstructions);
        updateVGpuResourceAbsoluteUtilization(gte, ((CustomVGpuSimple)vgpu).getGddram());
        updateVGpuResourceAbsoluteUtilization(gte, ((CustomVGpuSimple)vgpu).getBw());

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
}
