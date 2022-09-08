package org.cloudbus.cloudsim.gp.schedulers.gputask;

import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTaskExecution;
import org.gpucloudsimplus.listeners.GpuTaskResourceAllocationFailEventInfo;

import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.schedulers.MipsShare;

import java.io.Serializable;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//extends Serializable
public interface GpuTaskScheduler {
	Logger LOGGER = LoggerFactory.getLogger (GpuTaskScheduler.class.getSimpleName());
	
	GpuTaskScheduler NULL = new GpuTaskSchedulerNull();
	
	GpuTask gpuTaskFail (GpuTask gpuTask);
	
	GpuTask gpuTaskCancel (GpuTask gpuTask);

    boolean gpuTaskReady (GpuTask gpuTask);

    boolean gpuTaskPause (GpuTask gpuTask);
    
    double gpuTaskResume (GpuTask gpuTask);

    double gpuTaskSubmit (GpuTask gpuTask, double fileTransferTime);

    double gpuTaskSubmit (GpuTask gpuTask);
    
    List<GpuTaskExecution> getGpuTaskExecList ();

    <T extends GpuTask> List<T> getGpuTaskSubmittedList ();

    GpuTaskScheduler enableCloudletSubmittedList ();
    
    List<GpuTaskExecution> getGpuTaskWaitingList ();

    List<GpuTask> getGpuTaskList ();

    List<GpuTaskExecution> getGpuTaskFinishedList ();

    boolean isEmpty ();
    
    void deallocateCoresFromVGpu (long pcoreToRemove);

    double getCurrentRequestedBwPercentUtilization ();

    double getCurrentRequestedGddramPercentUtilization ();
    
    double getPreviousTime ();

    double getRequestedGpuPercent (double time);

    double getAllocatedGpuPercent (double time);
    
    boolean hasFinishedGpuTasks ();
////////////////////////////////////////////////////////////
    /**
     * Gets the {@link CloudletTaskScheduler} that will be used by this CloudletScheduler to process
     * {@link VmPacket}s to be sent or received by the Vm that is assigned to the
     * current CloudletScheduler.
     *
     * @return the CloudletTaskScheduler for this CloudletScheduler or {@link CloudletTaskScheduler#NULL} if this scheduler
     * will not deal with packets transmission.
     */
    CloudletTaskScheduler getTaskScheduler ();

    /**
     * Sets the {@link CloudletTaskScheduler} that will be used by this CloudletScheduler to process
     * {@link VmPacket}s to be sent or received by the Vm that is assigned to the
     * current CloudletScheduler. The Vm from the CloudletScheduler is also set to the CloudletTaskScheduler.
     *
     * <p><b>This attribute usually doesn't need to be set manually. See the note at the {@link CloudletTaskScheduler} interface for more details.</b></p>
     *
     * @param taskScheduler the CloudletTaskScheduler to set for this CloudletScheduler or {@link CloudletTaskScheduler#NULL} if this scheduler
     * will not deal with packets transmission.
     */
    void setTaskScheduler (CloudletTaskScheduler taskScheduler);

    /**
     * Checks if there is a {@link CloudletTaskScheduler} assigned to this CloudletScheduler
     * in order to enable tasks execution and dispatching packets from and to the Vm of this CloudletScheduler.
     * @return
     */
    boolean isThereTaskScheduler ();
//////////////////////////////////////////////////////////
    
    double updateProcessing (double currentTime, MipsShare mipsShare);

    VGpu getVGpu ();

    void setVGpu (VGpu vgpu);
    
    long getUsedCores ();

    long getFreeCores ();

	void addGpuTaskToReturnedList (GpuTask gpuTask);

    void clear();

    GpuTaskScheduler addOnGpuTaskResourceAllocationFail (
    		EventListener<GpuTaskResourceAllocationFailEventInfo> listener);

    boolean removeOnGpuTaskResourceAllocationFail (
    		EventListener<GpuTaskResourceAllocationFailEventInfo> listener);
}

