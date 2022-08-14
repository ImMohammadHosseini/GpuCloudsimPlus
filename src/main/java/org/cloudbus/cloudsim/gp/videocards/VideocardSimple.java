package org.cloudbus.cloudsim.gp.videocards;

public class VideocardSimple implements Videocard {
	
	private long id;
	private String type;
	private VGpuScheduler vgpuScheduler;
	private VideocardBwProvisioner pcieBwProvisioner;
	
	public VideocardSimple () {
		
	}
	
}

