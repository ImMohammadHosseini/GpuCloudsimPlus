package org.cloudbus.cloudsim.gp.vms;

import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.vms.Vm;

public interface GpuVm extends Vm {
	
	GpuVm NULL = new GpuVmNull();
	
	void setType (String type);
	
	String getType ();
	
	GpuVm setVGpu (VGpu vgpu);
	
	VGpu getVGpu ();
	
	boolean hasVGpu ();
}
