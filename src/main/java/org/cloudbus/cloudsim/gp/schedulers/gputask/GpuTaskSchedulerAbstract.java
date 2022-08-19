package org.cloudbus.cloudsim.gp.schedulers.gputask;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

import org.cloudbus.cloudsim.schedulers.MipsShare;
import java.util.*;


public abstract class GpuTaskSchedulerAbstract implements GpuTaskScheduler {
	
	private final List<CloudletExecution> gpuTaskPausedList;
    private final List<CloudletExecution> gpuTaskFinishedList;
    private final List<CloudletExecution> gpuTaskFailedList;
    private final List<CloudletExecution> gpuTaskExecList;
    private final List<CloudletExecution> gpuTaskWaitingList;
    
    private final List<GpuTask> gpuTaskSubmittedList;
    private final Set<GpuTask> gpuTaskReturnedList;
    
    //private CloudletTaskScheduler taskScheduler;
    private double previousTime;
    private MipsShare currentMipsShare;
    private boolean enableGpuTastSubmittedList;
    
    private CustomVGpu vgpu;
    
    private final List<EventListener<CloudletResourceAllocationFailEventInfo>> resourceAllocationFailListeners;

    protected GpuTaskSchedulerAbstract() {
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
}
