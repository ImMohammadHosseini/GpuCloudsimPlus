package org.cloudbus.cloudsim.gp.vms;

import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.vms.Vm;

public interface GpuVm extends Vm {
	
	GpuVm NULL = new GpuVmNull();
	
	void setType (String type);
	
	String getType ();
	
	GpuVm setVGpu (CustomVGpu vgpu);
	
	CustomVGpu getVGpu ();
	
	boolean hasVGpu ();
}
