package org.cloudbus.cloudsim.gp.schedulers.gpuvm;

import java.io.Serializable;


public interface GpuVmScheduler extends Serializable {
	GpuVmScheduler NULL = new GpuVmSchedulerNull();
}

