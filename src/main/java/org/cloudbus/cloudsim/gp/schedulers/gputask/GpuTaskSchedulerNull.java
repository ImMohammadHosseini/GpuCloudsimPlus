package org.cloudbus.cloudsim.gp.schedulers.gputask;

import java.util.List;
import java.util.Collections;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTaskExecution;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudsimplus.listeners.EventListener;
import org.gpucloudsimplus.listeners.GpuTaskResourceAllocationFailEventInfo;

public class GpuTaskSchedulerNull implements GpuTaskScheduler {

	@Override public GpuTask gpuTaskFail (GpuTask gpuTask) { return GpuTask.NULL; }
 	@Override public GpuTask gpuTaskCancel (GpuTask gpuTask) { return GpuTask.NULL; }
	@Override public boolean gpuTaskReady (GpuTask gpuTask) { return false; }
 	@Override public boolean gpuTaskPause (GpuTask gpuTask) { return false; }
 	@Override public double gpuTaskResume (GpuTask gpuTask) { return 0.0; }
 	@Override public double gpuTaskSubmit (GpuTask gpuTask, double fileTransferTime) { 
 		return 0.0;
	} 
	@Override public double gpuTaskSubmit (GpuTask gpuTask) { return 0.0; }
	@Override public List<GpuTaskExecution> getGpuTaskExecList () {
		return Collections.emptyList();
	}
	@Override public <T extends GpuTask> List<T> getGpuTaskSubmittedList () {
		return Collections.emptyList(); 
	}
	@Override public GpuTaskScheduler enableCloudletSubmittedList () { return this; }
	@Override public List<GpuTaskExecution> getGpuTaskWaitingList () {
		return Collections.emptyList();
	}
	@Override public List<GpuTask> getGpuTaskList () { return Collections.emptyList(); }
	@Override public List<GpuTaskExecution> getGpuTaskFinishedList () {
		return Collections.emptyList();
	}
	@Override public boolean isEmpty () { return false; }
	@Override public void deallocateCoresFromVGpu (long pcoreToRemove) { /**/ }
	@Override public double getCurrentRequestedBwPercentUtilization () { return 0.0; }
	@Override public double getCurrentRequestedGddramPercentUtilization () { return 0.0; }
	@Override public double getPreviousTime () { return 0.0; }
	@Override public double getRequestedGpuPercent (double time) { return 0.0; }
	@Override public double getAllocatedGpuPercent (double time) { return 0.0; }
	@Override public boolean hasFinishedGpuTasks () { return false; }
	@Override public double updateProcessing (double currentTime, MipsShare mipsShare) { 
		return 0.0;
	}
	@Override public VGpu getVGpu () { return VGpu.NULL; }
	@Override public void setVGpu (VGpu vgpu) { /**/ } 
	@Override public long getUsedCores () { return 0; } 
	@Override public long getFreeCores () { return 0; }
	@Override public void addGpuTaskToReturnedList (GpuTask gpuTask) { /**/ } 
	@Override public void clear () { /**/ } 
	@Override public GpuTaskScheduler addOnGpuTaskResourceAllocationFail (
			EventListener<GpuTaskResourceAllocationFailEventInfo> listener) {
		return this;
	}
	@Override public boolean removeOnGpuTaskResourceAllocationFail(
			EventListener<GpuTaskResourceAllocationFailEventInfo> listener) { 
		return false;
	}
	
}
