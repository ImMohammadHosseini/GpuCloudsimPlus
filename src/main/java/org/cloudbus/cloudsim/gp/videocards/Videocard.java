package org.cloudbus.cloudsim.gp.videocards;

import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;
import org.cloudbus.cloudsim.gp.provisioners.VideocardBwProvisioner;

public interface Videocard {
	
	Videocard NULL = new VideocardNull ();
	
	long getId ();
	
	void setId (long id);
	
	String getType ();
	
	void setType (String type);
	
	VGpuScheduler getVGpuScheduler ();
	
	Videocard setVGpuScheduler (VGpuScheduler vgpuScheduler);
	
	VideocardBwProvisioner getPcieBwProvisioner ();
	
	Videocard setPcieBwProvisioner (VideocardBwProvisioner pcieBwProvisioner);
}
