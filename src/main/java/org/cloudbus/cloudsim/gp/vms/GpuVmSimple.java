package org.cloudbus.cloudsim.gp.vms;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
//import org.cloudbus.cloudsim.core.CustomerEntityAbstract;

import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.schedulers.gpucloudlet.GpuCloudletScheduler;

public class GpuVmSimple extends VmSimple implements GpuVm {
	
	private String type;
	
	private VGpu vgpu;
	
	//private double arrivalTime;
	
	public GpuVmSimple (final Vm sourceVm, final VGpu vgpu, String type) {
		super (sourceVm);
		setVGpu(vgpu);
		setType (type);
	}
	
	public GpuVmSimple (final double mipsCapacity, final long numberOfPes, final VGpu vgpu,
			String type) {
		super (mipsCapacity, numberOfPes);
		setVGpu(vgpu);
		setType (type);
	}
	
	public GpuVmSimple (final double mipsCapacity, final long numberOfPes, 
			final GpuCloudletScheduler cloudletScheduler, final VGpu vgpu, String type) {
        super (mipsCapacity, numberOfPes, cloudletScheduler);
        setCloudletScheduler (cloudletScheduler);
        setVGpu(vgpu);
        setType (type);
	
	}
	
	public GpuVmSimple (final long id, final double mipsCapacity, final long numberOfPes, 
			final VGpu vgpu, String type) {
        super (id,  mipsCapacity, numberOfPes);
        setVGpu(vgpu);
        setType (type);
	}

	public GpuVmSimple (final long id, final long mipsCapacity, final long numberOfPes,
			final VGpu vgpu, String type) {
		super (id, mipsCapacity, numberOfPes);
		setVGpu(vgpu);
        setType (type);
	}

	@Override
	public void setType (String type) {
		this.type = type;
	}
	
	@Override
	public String getType () {
		return type;
	}
	
	@Override
	public GpuVmSimple setVGpu (VGpu vgpu) {
		this.vgpu = vgpu;
		if (vgpu.getGpuVm () == null)
			vgpu.setGpuVm (this);
		return this;
	}
	
	@Override
	public VGpu getVGpu () {
		return vgpu;
	}
	
	@Override
	public boolean hasVGpu () {
		return getVGpu() != null;
	}
}
