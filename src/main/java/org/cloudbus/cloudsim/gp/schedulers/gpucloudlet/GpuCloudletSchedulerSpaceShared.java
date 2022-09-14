package org.cloudbus.cloudsim.gp.schedulers.gpucloudlet;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;

import java.io.Serial;

public class GpuCloudletSchedulerSpaceShared extends GpuCloudletSchedulerAbstract {
	
	@Serial
    private static final long serialVersionUID = 4699085761507163349L;

    @Override
    public double cloudletResume(Cloudlet cloudlet) {
        return findCloudletInList(cloudlet, getCloudletPausedList())
            .map(this::movePausedCloudletToExecListOrWaitingList)
            .orElse(0.0);
    }

    private double movePausedCloudletToExecListOrWaitingList(final CloudletExecution cle) {
        getCloudletPausedList().remove(cle);

        if (isThereEnoughFreePesForCloudlet(cle)) {
            return movePausedCloudletToExecList(cle);
        }

        addCloudletToWaitingList(cle);
        return 0.0;
    }

    private double movePausedCloudletToExecList(final CloudletExecution cle) {
        addCloudletToExecList(cle);
        return cloudletEstimatedFinishTime(cle, getVm().getSimulation().clock());
    }

    @Override
    protected boolean canExecuteCloudletInternal(final CloudletExecution cle) {
        return isThereEnoughFreePesForCloudlet(cle);
    }
}
