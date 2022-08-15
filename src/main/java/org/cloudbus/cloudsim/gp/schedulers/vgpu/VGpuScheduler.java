package org.cloudbus.cloudsim.gp.schedulers.vgpu;


import java.io.Serializable;

public interface VGpuScheduler extends Serializable {
	
	VGpuScheduler NULL = new VGpuSchedulerNull();
}
