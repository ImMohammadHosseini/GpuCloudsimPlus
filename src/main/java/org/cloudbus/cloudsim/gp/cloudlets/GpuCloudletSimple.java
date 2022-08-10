package org.cloudbus.cloudsim.gp.cloudlets;

import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTaskSimple;

import java.util.Objects;

public class GpuCloudletSimple extends CloudletSimple {
	
	private GpuTaskSimple gpuTask;
	
	public GpuCloudletSimple (final long length, final int pesNumber, 
			final UtilizationModel utilizationModel, GpuTaskSimple gpuTask) {
		super (length, pesNumber, utilizationModel);
		setGpuTask (gpuTask);
	}
	
	public GpuCloudletSimple (final long length, final int pesNumber, GpuTaskSimple gpuTask) {
        super(length, pesNumber);
        setGpuTask (gpuTask);
	}
	
	public GpuCloudletSimple (final long length, final long pesNumber, GpuTaskSimple gpuTask) {
        super(length, pesNumber);
        setGpuTask (gpuTask);
    }
	
	public GpuCloudletSimple (final long id, final long length, final long pesNumber, 
			GpuTaskSimple gpuTask) {
        super(id, length, pesNumber);
        setGpuTask (gpuTask);
    }
	
	protected void setGpuTask (GpuTaskSimple gpuTask) {
		this.gpuTask = gpuTask;
		if (gpuTask != null && gpuTask.getGpuCloudlet() == null)
			gpuTask.setGpuCloudlet(this);
	}
	
	public GpuTaskSimple getGpuTask () {
		return gpuTask;
	}
}
