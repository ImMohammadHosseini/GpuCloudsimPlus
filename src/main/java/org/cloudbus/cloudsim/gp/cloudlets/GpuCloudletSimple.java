package org.cloudbus.cloudsim.gp.cloudlets;

import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;

import java.util.Objects;

public class GpuCloudletSimple extends CloudletSimple implements GpuCloudlet {
	
	private GpuTask gpuTask;
	
	public GpuCloudletSimple (final long length, final int pesNumber, 
			final UtilizationModel utilizationModel, GpuTask gpuTask) {
		super (length, pesNumber, utilizationModel);
		setGpuTask (gpuTask);
	}
	
	public GpuCloudletSimple (final long length, final int pesNumber, GpuTask gpuTask) {
        super(length, pesNumber);
        setGpuTask (gpuTask);
	}
	
	public GpuCloudletSimple (final long length, final long pesNumber, GpuTask gpuTask) {
        super(length, pesNumber);
        setGpuTask (gpuTask);
    }
	
	public GpuCloudletSimple (final long id, final long length, final long pesNumber, 
			GpuTask gpuTask) {
        super(id, length, pesNumber);
        setGpuTask (gpuTask);
    }
	
	@Override
	public GpuCloudlet setGpuTask (GpuTask gpuTask) {
		this.gpuTask = gpuTask;
		if (gpuTask != null && gpuTask.getGpuCloudlet() == null)
			gpuTask.setGpuCloudlet(this);
		return this;
	}
	
	@Override
	public GpuTask getGpuTask () {
		return gpuTask;
	}
}
