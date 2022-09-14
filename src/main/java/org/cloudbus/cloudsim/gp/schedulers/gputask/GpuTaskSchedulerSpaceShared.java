package org.cloudbus.cloudsim.gp.schedulers.gputask;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTaskExecution;

import java.io.Serial;

public class GpuTaskSchedulerSpaceShared extends GpuTaskSchedulerAbstract {
	@Serial
    private static final long serialVersionUID = 4699085761507163349L;

    @Override
    public double gpuTaskResume (GpuTask gpuTask) {
        return findGpuTaskInList(gpuTask, getGpuTaskPausedList())
            .map(this::movePausedGpuTaskToExecListOrWaitingList)
            .orElse(0.0);
    }

    private double movePausedGpuTaskToExecListOrWaitingList (final GpuTaskExecution gte) {
        getGpuTaskPausedList().remove(gte);

        if (isThereEnoughFreeCoresForGpuTask(gte)) {
            return movePausedGpuTaskToExecList(gte);
        }

        addGpuTaskToWaitingList(gte);
        return 0.0;
    }

    private double movePausedGpuTaskToExecList(final GpuTaskExecution gte) {
        addGpuTaskToExecList(gte);
        return gpuTaskEstimatedFinishTime(gte, getVGpu().getSimulation().clock());
    }

    @Override
    protected boolean canExecuteGpuTaskInternal(final GpuTaskExecution gte) {
        return isThereEnoughFreeCoresForGpuTask(gte);
    }
}
