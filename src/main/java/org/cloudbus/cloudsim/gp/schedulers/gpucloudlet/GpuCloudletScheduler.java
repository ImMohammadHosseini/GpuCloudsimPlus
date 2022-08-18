package org.cloudbus.cloudsim.gp.schedulers.gpucloudlet;

import java.io.Serializable;


public interface GpuCloudletScheduler extends Serializable {
	GpuCloudletScheduler NULL = new GpuCloudletSchedulerNull();
}

