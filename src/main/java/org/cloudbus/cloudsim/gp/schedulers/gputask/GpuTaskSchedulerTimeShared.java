package org.cloudbus.cloudsim.gp.schedulers.gputask;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTaskExecution;
import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;

import java.io.Serial;
import java.util.List;

public class GpuTaskSchedulerTimeShared extends GpuTaskSchedulerAbstract {
	@Serial
    private static final long serialVersionUID = 2115862129708036038L;

    @Override
    public List<GpuTaskExecution> getGpuTaskWaitingList() {
        //The method was overridden here just to extend its JavaDoc.
        return super.getGpuTaskWaitingList();
    }

    private double movePausedGpuTaskToExecListAndGetExpectedFinishTime (
    		final GpuTaskExecution gpuTask) {
        getGpuTaskPausedList().remove(gpuTask);
        addGpuTaskToExecList(gpuTask);
        return gpuTaskEstimatedFinishTime(gpuTask, getVGpu().getSimulation().clock());
    }

    @Override
    public double gpuTaskResume(final GpuTask gpuTask) {
        return findGpuTaskInList(gpuTask, getGpuTaskPausedList())
                .map(this::movePausedGpuTaskToExecListAndGetExpectedFinishTime)
                .orElse(0.0);
    }

    @Override
    protected boolean canExecuteGpuTaskInternal(final GpuTaskExecution gpuTask) {
        return true;
    }
}
