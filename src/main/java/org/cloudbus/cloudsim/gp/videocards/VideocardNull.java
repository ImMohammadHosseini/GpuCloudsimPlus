package org.cloudbus.cloudsim.gp.videocards;

import org.cloudbus.cloudsim.gp.provisioners.VideocardBwProvisioner;
import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;

public class VideocardNull implements Videocard {

	@Override public long getId () {
		return -1;
	}
	
	@Override public void setId (long id) {/**/}
	
	@Override public String getType () { return "";}
	
	@Override public void setType (String type) {/**/}
	
	@Override public VGpuScheduler getVGpuScheduler () {
		return VGpuScheduler.NULL;
	}
	
	@Override public Videocard setVGpuScheduler (VGpuScheduler vgpuScheduler) {
		return this;
	}
	
	@Override public VideocardBwProvisioner getPcieBwProvisioner () {
		return VideocardBwProvisioner.NULL;
	}
	
	@Override public Videocard setPcieBwProvisioner (VideocardBwProvisioner pcieBwProvisioner) {
		return this;
	}
}

