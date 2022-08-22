package org.cloudbus.cloudsim.gp.cloudlets;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;

import java.util.Objects;

public interface GpuCloudlet extends Cloudlet {
	GpuCloudlet NULL = new GpuCloudletNull();

	
	GpuCloudlet setGpuTask (GpuTask gpuTask);
	
	GpuTask getGpuTask ();
}
