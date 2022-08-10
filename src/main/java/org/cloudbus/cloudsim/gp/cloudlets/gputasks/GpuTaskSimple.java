package org.cloudbus.cloudsim.gp.cloudlets.gputasks;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudletSimple;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.EventListener;

import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.HashSet;

import static java.util.Objects.requireNonNull;

public class GpuTaskSimple implements GpuTask {
	
    private long taskId;
    private GpuCloudletSimple gpuCloudlet;
    private long blockLength;
    private long finishedLengthSoFar;
    private long numberOfPes;
    private Status status;
    //private boolean returnedToBroker;
    private double execStartTime;
    private int priority;
    ///private int netServiceLevel;
    private List<String> requiredFiles;
    private long fileSize;
    private long outputSize;
    private double finishTime;
    private UtilizationModel utilizationModelGpu;
    private UtilizationModel utilizationModelGddram;
    private UtilizationModel utilizationModelBw;

    private final Set<EventListener<CloudletVmEventInfo>> onStartListeners;
    private final Set<EventListener<CloudletVmEventInfo>> onFinishListeners;
    private final Set<EventListener<CloudletVmEventInfo>> onUpdateProcessingListeners;

    private double submissionDelay;
    private double lifeTime;
    private double arrivalTime;
    
    public GpuTaskSimple (final long id, final long blockLength, final long numberOfPes) {
    	
    	this.requiredFiles = new LinkedList<>();
        this.setTaskId(id);
        this.setNumberOfPes(numberOfPes);
        this.setBlockLength(blockLength);
        this.setFileSize(1);
        this.setOutputSize(1);
        this.setSubmissionDelay(0.0);
        this.setArrivalTime(-1);

        this.reset();

        setUtilizationModelGpu(new UtilizationModelFull());
        setUtilizationModelGddram(UtilizationModel.NULL);
        setUtilizationModelBw(UtilizationModel.NULL);
        onStartListeners = new HashSet<>();
        onFinishListeners = new HashSet<>();
        onUpdateProcessingListeners = new HashSet<>();
    }
    
    public GpuTaskSimple (final long length, final int pesNumber, 
    		final UtilizationModel utilizationModel) {
        this(-1, length, pesNumber);
        setUtilizationModel(utilizationModel);
    }
    
    public GpuTaskSimple (final long length, final int pesNumber) {
        this(-1, length, pesNumber);
    }
    
    public GpuTaskSimple (final long length, final long pesNumber) {
        this(-1, length, pesNumber);
    }
    
    @Override
    public String toString() {
        return String.format("GpuCloudlet %d , GpuTask %d ", gpuCloudlet.getId(), getTaskId());
    }
    
    @Override
    public final GpuTask reset() {
        //this.netServiceLevel = 0;
        this.execStartTime = 0.0;
        this.status = Status.INSTANTIATED;
        this.priority = 0;
        //setBroker(DatacenterBroker.NULL);
        setFinishTime(NOT_ASSIGNED); // meaning this Cloudlet hasn't finished yet
        this.gpuCloudlet = (GpuCloudletSimple) GpuCloudletSimple.NULL;//vm
        setExecStartTime(0.0);
        setArrivalTime(0);
        setCreationTime(0);
        setLifeTime(-1);

        this.setLastTriedDatacenter(Datacenter.NULL);
        return this;
    }
    
    @Override
    public final GpuTask setBlockLength (final long length) {
    	if (length == 0) {
            throw new IllegalArgumentException("GpuTask blockLength cannot be zero.");
        }
        this.blockLength = length;
        return this;
    }
    
    @Override
    public long getBlockLength() {
        return blockLength;
    }
    
    @Override
    public final GpuTask setNumberOfPes(final long numberOfPes) {
        if (numberOfPes <= 0) {
            throw new IllegalArgumentException("GpuTask number of PEs has to be greater than zero.");
        }
        this.numberOfPes = numberOfPes;
        return this;
    }

    @Override
    public long getNumberOfPes() {
        return numberOfPes;
    }
    
    @Override
    public GpuTask setPriority (final int priority) {
    	this.priority = priority;
        return this;
    }

    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public double getWaitingTime () {
    	return arrivalTime == -1 ? -1 : execStartTime - arrivalTime;
    }
    
    @Override
    public boolean addFinishedLengthSoFar (final long partialFinishedMI) {
    	if (partialFinishedMI < 0.0 || arrivalTime == -1) {
            return false;
        }

        final long maxLengthToAdd = getBlockLength() < 0 ?
                                    partialFinishedMI :
                                    Math.min(partialFinishedMI, absLength()-getFinishedLengthSoFar());
        finishedLengthSoFar += maxLengthToAdd;
        returnToBrokerIfFinished();//must be changed
        return true;
    }
    
    @Override
    public long getFinishedLengthSoFar () {
    	if(getBlockLength() > 0) 
            return Math.min(finishedLengthSoFar, absLength());
    	
    	return finishedLengthSoFar;
    }

    @Override
    public boolean isFinished() {
        return (getLifeTime() > 0 && getActualGpuTime() >= getLifeTime()) ||
               (getLength() > 0 && getFinishedLengthSoFar() >= getLength());
    }

    @Override
    public final GpuTask setFileSize(final long fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("GpuTask file size has to be greater than zero.");
        }
        this.fileSize = fileSize;
        return this;
    }
    
    @Override
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public final GpuTask setOutputSize(final long outputSize) {
        if (outputSize <= 0) {
            throw new IllegalArgumentException("GpuTask output size has to be greater than zero.");
        }
        this.outputSize = outputSize;
        return this;
    }
    
    @Override
    public long getOutputSize() {
        return outputSize;
    }
    
    protected final void setArrivalTime(final double arrivalTime) {
        if(arrivalTime < 0)
            this.arrivalTime = -1;
        else this.arrivalTime = arrivalTime;
    }
    
    @Override
    public double getArrivalTime() {
        return arrivalTime;
    }

    @Override
    public void setExecStartTime(final double clockTime) {
        final boolean isStartingInSomeVm = this.execStartTime <= 0 && clockTime > 0 && vm != Vm.NULL && vm != null;
        this.execStartTime = clockTime;
        if(isStartingInSomeVm){
            onStartListeners.forEach(listener -> listener.update(CloudletVmEventInfo.of(listener, clockTime, this)));
        }
    }
    
    @Override
    public double getExecStartTime() {
        return execStartTime;
    }
    
    @Override
    public boolean setStatus(final Status newStatus) {
        if (this.status == newStatus) {
            return false;
        }

        if (newStatus == Status.SUCCESS) {
            setFinishTime(getSimulation().clock());
        }

        this.status = newStatus;
        return true;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public long getGpuTaskTotalLength() {
        return getBlockLength() * getNumberOfPes();
    }
    
    @Override
    public double getActualGpuTime () {
    	final double time = getFinishTime() == NOT_ASSIGNED ? getSimulation().clock() : finishTime;
        return time - execStartTime;
    }
    
    protected final void setFinishTime(final double finishTime) {
        this.finishTime = finishTime;
    }
    
    @Override
    public double getFinishTime () {
    	return finishTime;
    }
    
    @Override
    public void setTaskId (final long taskId) {
    	this.taskId = taskId;
    }
    
    @Override
    public long getTaskId () {
    	return taskId;
    }
    
    @Override
    public GpuCloudletSimple getGpuCloudlet () {
    	return gpuCloudlet;
    }
    
    @Override
	public void setGpuCloudlet (GpuCloudletSimple gpuCloudlet) {
		this.gpuCloudlet = gpuCloudlet;
		
		if (gpuCloudlet.getGpuTask() == null)
			gpuCloudlet.setGpuTask(this);
	}

    @Override
    public GpuTask setUtilizationModel (UtilizationModel utilizationModel) {
    	setUtilizationModelBw(utilizationModel);
        setUtilizationModelGddram(utilizationModel);
        setUtilizationModelGpu(utilizationModel);
        return this;
    }

    @Override
    public GpuTask setUtilizationModelBw (UtilizationModel utilizationModelBw) {
    	this.utilizationModelBw = requireNonNull(utilizationModelBw);
        return this;
    }
    
    @Override
    public UtilizationModel getUtilizationModelBw() {
        return utilizationModelBw;
    }
    
    @Override
    public double getUtilizationOfBw() {
        return getUtilizationOfBw(getSimulation().clock());
    }
    
    @Override
    public double getUtilizationOfBw(final double time) {
        return getUtilizationModelBw().getUtilization(time);
    }

    @Override
    public GpuTask setUtilizationModelGpu (UtilizationModel utilizationModelGpu) {
    	this.utilizationModelGpu = requireNonNull(utilizationModelGpu);
        return this;
    }
    
    @Override
    public UtilizationModel getUtilizationModelGpu() {
        return utilizationModelGpu;
    }
    
    @Override
    public double getUtilizationOfGpu() {
        return getUtilizationOfCpu(getSimulation().clock());
    }
    
    @Override
    public double getUtilizationOfGpu(final double time) {
        return getUtilizationModelGpu().getUtilization(time);
    }

    @Override
    public GpuTask setUtilizationModelGddram (UtilizationModel utilizationModelGddram) {
    	this.utilizationModelGddram = requireNonNull(utilizationModelGddram);
        return this;
    }
    
    @Override
    public UtilizationModel getUtilizationModelGddram() {
        return utilizationModelGddram;
    }

    @Override
    public double getUtilizationOfGddram() {
        return getUtilizationOfRam(getSimulation().clock());
    }

    @Override
    public double getUtilizationOfGddram(final double time) {
        return getUtilizationModelGddram().getUtilization(time);
    }
    
    
    
    
    
    
    

    
    
}
