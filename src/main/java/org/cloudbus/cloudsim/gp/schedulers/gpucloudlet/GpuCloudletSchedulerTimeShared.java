package org.cloudbus.cloudsim.gp.schedulers.gpucloudlet;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;

import java.io.Serial;

import java.util.*;

public class GpuCloudletSchedulerTimeShared extends GpuCloudletSchedulerAbstract {
	@Serial
    private static final long serialVersionUID = 2115862129708036038L;

    @Override
    public List<CloudletExecution> getCloudletWaitingList() {
        //The method was overridden here just to extend its JavaDoc.
        return super.getCloudletWaitingList();
    }

    private double movePausedCloudletToExecListAndGetExpectedFinishTime(final CloudletExecution cloudlet) {
        getCloudletPausedList().remove(cloudlet);
        addCloudletToExecList(cloudlet);
        return cloudletEstimatedFinishTime(cloudlet, getVm().getSimulation().clock());
    }

    @Override
    public double cloudletResume(final Cloudlet cloudlet) {
        return findCloudletInList(cloudlet, getCloudletPausedList())
                .map(this::movePausedCloudletToExecListAndGetExpectedFinishTime)
                .orElse(0.0);
    }

    @Override
    protected boolean canExecuteCloudletInternal(final CloudletExecution cloudlet) {
        return true;
    }
}
