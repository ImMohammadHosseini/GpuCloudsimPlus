package org.cloudbus.cloudsim.gp.videocards;

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
