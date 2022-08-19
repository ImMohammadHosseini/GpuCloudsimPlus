package org.cloudbus.cloudsim.gp.cloudlets.gputasks;

import org.cloudbus.cloudsim.util.Conversion;

public class GpuTaskExecution {
	
	public static final GpuTaskExecution NULL = new GpuTaskExecution(GpuTask.NULL);
	
    private final GpuTask gpuTask;
	private double fileTransferTime;
    private final double arrivalTime;
    private double finishedTime;
    private double overSubscriptionDelay;
    private double finishRequestTime;
    private long taskFinishedSoFar;
    private double startExecTime;
	private double lastProcessingTime;
    private double totalCompletionTime;
    private double virtualRuntime;
    private double timeSlice;
    private double lastAllocatedMips;
    private double wallClockTime;
    
    public GpuTaskExecution (final GpuTask gpuTask) {
        this.gpuTask = gpuTask;
        this.arrivalTime = gpuTask.registerArrivalInDatacenter();
        this.finishedTime = gpuTask.NOT_ASSIGNED;
        this.lastProcessingTime = gpuTask.NOT_ASSIGNED;
        this.totalCompletionTime = 0.0;
        this.startExecTime = 0.0;
        this.virtualRuntime = 0;
        this.taskFinishedSoFar = gpuTask.getFinishedLengthSoFar() * Conversion.MILLION;
    }
    
    public long getGpuTaskLength () {
        return gpuTask.getBlockLength();
    }
    
    public long getNumberOfCores (){
        return gpuTask.getNumberOfCores ();
    }
    
    public boolean setStatus (final GpuTask.Status newStatus) {
        final GpuTask.Status prevStatus = gpuTask.getStatus();

        if (prevStatus.equals(newStatus)) {
            return false;
        }

        final double clock = gpuTask.getSimulation().clock();
        gpuTask.setStatus(newStatus);

        if (prevStatus == GpuTask.Status.INEXEC && isNotRunning(newStatus)) {
            totalCompletionTime += clock - startExecTime;
            return true;
        }

        if (prevStatus == GpuTask.Status.RESUMED && newStatus == GpuTask.Status.SUCCESS) {
            totalCompletionTime += clock - startExecTime;
            return true;
        }

        startOrResumeGpuTask(newStatus, prevStatus);
        return true;
    }
    
    private void startOrResumeGpuTask (final GpuTask.Status newStatus, 
    		final GpuTask.Status oldStatus) {
    	
        final double clock = gpuTask.getSimulation().clock();
        if (newStatus == GpuTask.Status.INEXEC || 
        		isTryingToResumePausedGpuTask(newStatus, oldStatus)) {
            startExecTime = clock;
            if(gpuTask.getExecStartTime() == 0) {
            	gpuTask.setExecStartTime(startExecTime);
            }
        }
    }
    
    private boolean isTryingToResumePausedGpuTask (final GpuTask.Status newStatus, 
    		final GpuTask.Status oldStatus) {
    	
        return newStatus == GpuTask.Status.RESUMED && oldStatus == GpuTask.Status.PAUSED;
    }
    
    private static boolean isNotRunning (final GpuTask.Status status) {
        return status == GpuTask.Status.CANCELED ||
               status == GpuTask.Status.PAUSED ||
               status == GpuTask.Status.SUCCESS;
    }
    
    public long getRemainingCloudletLength() {
        final long absLength = Math.abs(gpuTask.getBlockLength());
        final double miFinishedSoFar = taskFinishedSoFar / (double) Conversion.MILLION;

        if(gpuTask.getBlockLength() > 0){
            return (long)Math.max(absLength - miFinishedSoFar, 0);
        }
        
        if(absLength-miFinishedSoFar == 0) {
            return absLength;
        }
        return (long)Math.min(Math.abs(absLength-miFinishedSoFar), absLength);
    }
    
    public void finalizeCloudlet() {
        this.wallClockTime = gpuTask.getSimulation().clock() - arrivalTime;

        final long finishedLengthMI = taskFinishedSoFar / Conversion.MILLION;
        gpuTask.addFinishedLengthSoFar(finishedLengthMI - gpuTask.getFinishedLengthSoFar());
    }

    
    
}
