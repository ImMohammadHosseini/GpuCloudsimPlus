package org.cloudbus.cloudsim.gp.vms;

import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.vms.Vm;

public interface CustomGpuVm extends Vm {
	
	CustomGpuVm NULL = new CustomGpuVmNull();
	
	void setType (String type);
	
	String getType ();
	
	CustomGpuVmSimple setVGpu (CustomVGpu vgpu);
	
	CustomVGpu getVGpu ();
}
