package org.cloudbus.cloudsim.gp.schedulers.gpucloudlet;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;
import org.cloudbus.cloudsim.resources.Pe;

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

        // it can go to the exec list
        if (isThereEnoughFreePesForCloudlet(cle)) {
            return movePausedCloudletToExecList(cle);
        }

        // No enough free PEs: go to the waiting queue
        /*
         * A resumed cloudlet is not immediately added to the execution list.
         * It is queued so that the next time the scheduler process VM execution,
         * the cloudlet may have the opportunity to run.
         * It goes to the end of the waiting list because other cloudlets
         * could be waiting longer and have priority to execute.
         */
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
