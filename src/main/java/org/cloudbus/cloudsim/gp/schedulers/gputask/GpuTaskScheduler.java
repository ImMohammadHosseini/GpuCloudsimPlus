package org.cloudbus.cloudsim.gp.schedulers.gputask;

import java.io.Serializable;


public interface GpuTaskScheduler extends Serializable {
	GpuTaskScheduler NULL = new GpuTaskSchedulerNull();
}

