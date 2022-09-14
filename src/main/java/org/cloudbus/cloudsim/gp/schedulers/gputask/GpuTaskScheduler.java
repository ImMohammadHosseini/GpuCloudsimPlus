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
    //CloudletTaskScheduler getTaskScheduler ();

    //void setTaskScheduler (CloudletTaskScheduler taskScheduler);

    //boolean isThereTaskScheduler ();
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

