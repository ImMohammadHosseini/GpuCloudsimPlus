package org.cloudbus.cloudsim.gp.schedulers.gpucloudlet;

import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.util.Conversion;
import org.cloudbus.cloudsim.core.CloudSimTag;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerAbstract;
import org.cloudbus.cloudsim.schedulers.cloudlet.network.CloudletTaskScheduler;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.vms.GpuVmSimple;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudlet;
import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenter;

import org.cloudsimplus.listeners.CloudletResourceAllocationFailEventInfo;
import org.cloudsimplus.listeners.EventListener;

import java.io.Serial;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.cloudsimplus.listeners.CloudletResourceAllocationFailEventInfo.of;


public abstract class GpuCloudletSchedulerAbstract extends CloudletSchedulerAbstract 
implements GpuCloudletScheduler {
	
	/*@Serial
    private static final long serialVersionUID = -2314361120790372742L;

    private final List<CloudletExecution> cloudletPausedList;
    private final List<CloudletExecution> cloudletFinishedList;
    private final List<CloudletExecution> cloudletFailedList;
    private final List<CloudletExecution> cloudletExecList;
    private final List<CloudletExecution> cloudletWaitingList;

    private CloudletTaskScheduler taskScheduler;

    private double previousTime;

    private MipsShare currentMipsShare;
    private boolean enableCloudletSubmittedList;
    private final List<GpuCloudlet> cloudletSubmittedList;
    private final Set<GpuCloudlet> cloudletReturnedList;

    private GpuVm gpuVm;

    private final List<EventListener<CloudletResourceAllocationFailEventInfo>> resourceAllocationFailListeners;
	*/
    protected GpuCloudletSchedulerAbstract () {
    	super ();
        /*setPreviousTime(0.0);
        gpuVm = GpuVm.NULL;
        cloudletSubmittedList = new ArrayList<>();
        cloudletExecList = new ArrayList<>();
        cloudletPausedList = new ArrayList<>();
        cloudletFinishedList = new ArrayList<>();
        cloudletFailedList = new ArrayList<>();
        cloudletWaitingList = new ArrayList<>();
        cloudletReturnedList = new HashSet<>();
        currentMipsShare = new MipsShare();
        taskScheduler = CloudletTaskScheduler.NULL;
        resourceAllocationFailListeners = new ArrayList<>();*/
    }
    
    /*@Override
    public double getPreviousTime () {
        return previousTime;
    }

    protected final void setPreviousTime (final double previousTime) {
        this.previousTime = previousTime;
    }

    public MipsShare getCurrentMipsShare () {
        return currentMipsShare;
    }
    
    protected void setCurrentMipsShare (final MipsShare currentMipsShare) {
        if(currentMipsShare.pes() > gpuVm.getNumberOfPes()){
            LOGGER.warn("Requested {} PEs but {} has just {}", currentMipsShare.pes(), gpuVm, 
            		gpuVm.getNumberOfPes());
            this.currentMipsShare = new MipsShare(gpuVm.getNumberOfPes(), currentMipsShare.mips());
        }
        else this.currentMipsShare = currentMipsShare;
    }
    
    public double getAvailableMipsByPe () {
        final long totalPesOfAllExecCloudlets = totalPesOfAllExecCloudlets();
        if(totalPesOfAllExecCloudlets > currentMipsShare.pes()) {
            return getTotalMipsShare() / totalPesOfAllExecCloudlets;
        }

        return getPeCapacity();
    }

    private Double getPeCapacity () {
        return currentMipsShare.mips();
    }
    
    private long totalPesOfAllExecCloudlets () {
        return cloudletExecList.stream()
            .map(CloudletExecution::getCloudlet)
            .mapToLong(Cloudlet::getNumberOfPes).sum();
    }

    private double getTotalMipsShare () {
        return currentMipsShare.totalMips();
    }
    
    @Override
    public List<CloudletExecution> getCloudletExecList () {
        return Collections.unmodifiableList(cloudletExecList);
    }

    @Override
    public <T extends Cloudlet> List<T> getCloudletSubmittedList () {
        if(cloudletSubmittedList.isEmpty() && !enableCloudletSubmittedList) {
            LOGGER.warn("{}: The list of submitted Cloudlets for {} is empty maybe because "
            		+ "you didn't enabled it by calling enableCloudletSubmittedList().", 
            		getClass().getSimpleName(), gpuVm);
        }

        return (List<T>) cloudletSubmittedList;
    }

    @Override
    public GpuCloudletScheduler enableCloudletSubmittedList () {
        this.enableCloudletSubmittedList = true;
        return this;
    }

    protected void addCloudletToWaitingList (final CloudletExecution cle) {
        if(requireNonNull(cle) == CloudletExecution.NULL){
            return;
        }

        if(cle.getCloudlet().getStatus() != GpuCloudlet.Status.FROZEN) {
            cle.setStatus(GpuCloudlet.Status.QUEUED);
        }
        cloudletWaitingList.add(cle);
    }
    
    protected List<CloudletExecution> getCloudletPausedList () {
        return cloudletPausedList;
    }

    @Override
    public List<CloudletExecution> getCloudletFinishedList () {
        return cloudletFinishedList;
    }

    protected List<CloudletExecution> getCloudletFailedList () {
        return cloudletFailedList;
    }

    @Override
    public List<CloudletExecution> getCloudletWaitingList () {
        return Collections.unmodifiableList(cloudletWaitingList);
    }

    protected void sortCloudletWaitingList (final Comparator<CloudletExecution> comparator){
        cloudletWaitingList.sort(comparator);
    }

    @Override
    public final double cloudletSubmit (final Cloudlet cloudlet) {
        return cloudletSubmit(cloudlet, 0.0);
    }

    @Override
    public final double cloudletSubmit (final Cloudlet cloudlet, final double fileTransferTime) {
        if(enableCloudletSubmittedList) {
            cloudletSubmittedList.add((GpuCloudlet)cloudlet);
        }

        return cloudletSubmitInternal (new CloudletExecution(cloudlet), fileTransferTime);
    }

    protected double cloudletSubmitInternal (final CloudletExecution cle, 
    		final double fileTransferTime) {
        if (canExecuteCloudlet(cle)) {
            cle.setStatus(GpuCloudlet.Status.INEXEC);
            cle.setFileTransferTime(fileTransferTime);
            addCloudletToExecList(cle);
            return fileTransferTime + Math.abs(cle.getCloudletLength()/getPeCapacity()) ;
        }

        // No enough free PEs, then add Cloudlet to the waiting queue
        addCloudletToWaitingList(cle);
        return 0.0;
    }

    protected void addCloudletToExecList (final CloudletExecution cle) {
        cle.setStatus(Cloudlet.Status.INEXEC);
        cle.setLastProcessingTime(getVm().getSimulation().clock());
        cloudletExecList.add(cle);
        addUsedPes(cle.getNumberOfPes());
    }

    @Override
    public boolean hasFinishedCloudlets () {
        return !cloudletFinishedList.isEmpty();
    }

    protected Optional<CloudletExecution> findCloudletInAllLists (final double cloudletId) {
        final var cloudletExecInfoListStream = Stream.of(
            cloudletExecList, cloudletPausedList, cloudletWaitingList,
            cloudletFinishedList, cloudletFailedList
        );

        return cloudletExecInfoListStream
            .flatMap(List::stream)
            .filter(cle -> cle.getCloudletId() == cloudletId)
            .findFirst();
    }

    protected Optional<CloudletExecution> findCloudletInList (final Cloudlet cloudlet, 
    		final List<CloudletExecution> list) {
        return list.stream()
            .filter(cle -> cle.getCloudletId() == cloudlet.getId())
            .findFirst();
    }

    protected void cloudletFinish (final CloudletExecution cle) {
        cle.setStatus(GpuCloudlet.Status.SUCCESS);
        cle.finalizeCloudlet();
        cloudletFinishedList.add(cle);
    }

    @Override
    public boolean cloudletReady (final Cloudlet cloudlet) {
        if (changeStatusOfCloudletIntoList(cloudletPausedList, cloudlet, this::changeToReady)) {
            return true;
        }

        //*
         If the Cloudlet was not found in the paused list, it hasn't started executing yet.
         It may have been frozen waiting for a READY message.
         This way, just changes its status to ready so
         //that it can be scheduled naturally to start executing.
        
        cloudlet.setStatus(GpuCloudlet.Status.READY);

        /*
         Requests a cloudlet processing update to ensure the cloudlet will be moved to the
         exec list as soon as possible.
         Without such a request, the Cloudlet may start executing only
         after a new and possibly unrelated message is processed by the simulator.
         Since the next message to be received may take a long time,
         //the processing update is requested right away.
         
        final Datacenter dc = gpuVm.getHost().getDatacenter();
        dc.schedule(CloudSimTag.VM_UPDATE_CLOUDLET_PROCESSING);
        return true;
    }

    private void changeToReady(final CloudletExecution cle) {
        changeStatusOfCloudlet(cle, cle.getCloudlet().getStatus(), GpuCloudlet.Status.READY);
    }

    @Override
    public boolean cloudletPause(final Cloudlet cloudlet) {
        if (changeStatusOfCloudletIntoList(cloudletExecList, cloudlet, this::changeInExecToPaused)) {
            return true;
        }

        return changeStatusOfCloudletIntoList(cloudletWaitingList, cloudlet, this::changeReadyToPaused);
    }

    private void changeInExecToPaused(final CloudletExecution cle) {
        changeStatusOfCloudlet(cle, GpuCloudlet.Status.INEXEC, GpuCloudlet.Status.PAUSED);
        removeUsedPes(cle.getNumberOfPes());
    }

    private void changeReadyToPaused(final CloudletExecution cle) {
        changeStatusOfCloudlet(cle, GpuCloudlet.Status.READY, GpuCloudlet.Status.PAUSED);
    }

    @Override
    public Cloudlet cloudletFail(final Cloudlet cloudlet) {
        return stopCloudlet(cloudlet, GpuCloudlet.Status.FAILED);
    }

    @Override
    public Cloudlet cloudletCancel(final Cloudlet cloudlet) {
        return stopCloudlet(cloudlet, GpuCloudlet.Status.CANCELED);
    }

    private Cloudlet stopCloudlet (final Cloudlet cloudlet, final GpuCloudlet.Status stopStatus) {
        //Removes finished cloudlets from the list without changing its status
        boolean found = changeStatusOfCloudletIntoList(cloudletFinishedList, cloudlet, cle -> {});
        if (found) {
            return cloudlet;
        }

        found = changeStatusOfCloudletIntoList(
            cloudletExecList, cloudlet,
            cle -> changeStatusOfCloudlet(cle, GpuCloudlet.Status.INEXEC, stopStatus));
        if (found) {
            return cloudlet;
        }

        found = changeStatusOfCloudletIntoList(
            cloudletPausedList, cloudlet,
            cle -> changeStatusOfCloudlet(cle, GpuCloudlet.Status.PAUSED, stopStatus));
        if (found) {
            return cloudlet;
        }

        changeStatusOfCloudletIntoList(
            cloudletWaitingList, cloudlet,
            cle -> changeStatusOfCloudlet(cle, Status.READY, stopStatus));
        if (found) {
            return cloudlet;
        }

        return Cloudlet.NULL;
    }

    private void changeStatusOfCloudlet (final CloudletExecution cle, 
    		final GpuCloudlet.Status currentStatus, final GpuCloudlet.Status newStatus) {
        if ((currentStatus == GpuCloudlet.Status.INEXEC || 
        		currentStatus == GpuCloudlet.Status.READY) && cle.getCloudlet().isFinished())
            cloudletFinish(cle);
        else cle.setStatus(newStatus);

        if (newStatus == GpuCloudlet.Status.PAUSED)
            cloudletPausedList.add(cle);
        else if (newStatus == GpuCloudlet.Status.READY)
            addCloudletToWaitingList(cle);
    }

    private boolean changeStatusOfCloudletIntoList(
        final List<CloudletExecution> cloudletList,
        final Cloudlet cloudlet,
        final Consumer<CloudletExecution> cloudletStatusUpdaterConsumer)
    {
        final Function<CloudletExecution, Cloudlet> removeCloudletAndUpdateStatus = cle -> {
            cloudletList.remove(cle);
            cloudletStatusUpdaterConsumer.accept(cle);
            return cle.getCloudlet();
        };

        return findCloudletInList(cloudlet, cloudletList)
            .map(removeCloudletAndUpdateStatus)
            .isPresent();
    }

    @Override
    public double updateProcessing(final double currentTime, final MipsShare mipsShare) {
        setCurrentMipsShare(mipsShare);

        if (isEmpty()) {
            setPreviousTime(currentTime);
            return Double.MAX_VALUE;
        }

        deallocateVmResources();

        double nextSimulationDelay = updateCloudletsProcessing(currentTime);
        nextSimulationDelay = Math.min(nextSimulationDelay, moveNextCloudletsFromWaitingToExecList(
        		currentTime));
        addCloudletsToFinishedList();

        setPreviousTime(currentTime);
        gpuVm.getSimulation().setLastCloudletProcessingUpdate(currentTime);

        return nextSimulationDelay;
    }

    private void deallocateVmResources() {
        ((GpuVmSimple)gpuVm).getRam().deallocateAllResources();
        ((GpuVmSimple)gpuVm).getBw().deallocateAllResources();
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private double updateCloudletsProcessing(final double currentTime) {
        double nextCloudletFinishTime = Double.MAX_VALUE;
        long usedPes = 0;
        //* Uses an indexed for to avoid ConcurrentModificationException,
        //* e.g., in cases when Cloudlet is cancelled during simulation execution. 
        for (int i = 0; i < cloudletExecList.size(); i++) {
            final CloudletExecution cle = cloudletExecList.get(i);
            updateCloudletProcessingAndPacketsDispatch(cle, currentTime);
            nextCloudletFinishTime = Math.min(nextCloudletFinishTime, cloudletEstimatedFinishTime(
            		cle, currentTime));
            usedPes += cle.getCloudlet().getNumberOfPes();
        }

        ((GpuVmSimple) gpuVm).setFreePesNumber(gpuVm.getNumberOfPes() - usedPes);

        return nextCloudletFinishTime;
    }

    private void updateCloudletProcessingAndPacketsDispatch (final CloudletExecution cle, 
    		final double currentTime) {
        long partialFinishedMI = 0;
        if (taskScheduler.isTimeToUpdateCloudletProcessing(cle.getCloudlet())) {
            partialFinishedMI = updateCloudletProcessing(cle, currentTime);
        }

        taskScheduler.processCloudletTasks(cle.getCloudlet(), partialFinishedMI);
    }

    protected long updateCloudletProcessing (final CloudletExecution cle, 
    		final double currentTime) {
        final double partialFinishedInstructions = cloudletExecutedInstructionsForTimeSpan(
        		cle, currentTime);
        cle.updateProcessing(partialFinishedInstructions);
        updateVmResourceAbsoluteUtilization(cle, ((GpuVmSimple)gpuVm).getRam());
        updateVmResourceAbsoluteUtilization(cle, ((GpuVmSimple)gpuVm).getBw());

        return (long)(partialFinishedInstructions/ Conversion.MILLION);
    }

    private void updateVmResourceAbsoluteUtilization (final CloudletExecution cle, 
    		final ResourceManageable vmResource) {
        final var cloudlet = cle.getCloudlet();
        final long requested = (long) getCloudletResourceAbsoluteUtilization(cloudlet, vmResource);
        if(requested > vmResource.getCapacity()){
            LOGGER.warn(
                "{}: {}: {} requested {} {} of {} but that is >= the VM capacity ({})",
                gpuVm.getSimulation().clockStr(), getClass().getSimpleName(), cloudlet,
                requested, vmResource.getUnit(), vmResource.getClass().getSimpleName(), 
                vmResource.getCapacity());
            return;
        }

        final long available = vmResource.getAvailableResource();
        if(requested > available){
            final String msg1 =
                    available > 0 ?
                    String.format("just %d was available", available):
                    "no amount is available.";
            final String msg2 = vmResource.getClass() == Ram.class ? ". Using Virtual Memory," 
            		: ",";
            LOGGER.warn(
                "{}: {}: {} requested {} {} of {} but {}{} which delays Cloudlet processing.",
                gpuVm.getSimulation().clockStr(), getClass().getSimpleName(), cloudlet,
                requested, vmResource.getUnit(), vmResource.getClass().getSimpleName(), 
                msg1, msg2);

            updateOnResourceAllocationFailListeners(vmResource, cloudlet, requested, available);
        }

        vmResource.allocateResource(Math.min(requested, available));
    }
    
    
    
    
    
    
    
    @Override
    public List<Cloudlet> getCloudletCreatedList () {
    	return getGpuCloudletCreatedList();
    }
    
    public <T extends Cloudlet> List<T> getGpuCloudletCreatedList () {
    	return (List<T>) gpucloudletsCreatedList;
    }*/
}
