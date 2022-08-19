package org.cloudbus.cloudsim.gp.schedulers.gputask;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTaskExecution;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

import org.cloudsimplus.listeners.EventListener;
import org.gpucloudsimplus.listeners.GpuTaskResourceAllocationFailEventInfo;


import org.cloudbus.cloudsim.schedulers.MipsShare;
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
