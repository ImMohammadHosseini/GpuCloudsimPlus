package org.cloudbus.cloudsim.gp.core;

import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.videocards.Videocard;

public interface GpuResourceStatsComputer<T extends GResourceStats> {
	
	T getGpuUtilizationStats();

    void enableUtilizationStats();
}