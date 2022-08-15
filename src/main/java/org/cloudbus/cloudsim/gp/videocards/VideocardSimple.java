package org.cloudbus.cloudsim.gp.videocards;

import org.cloudbus.cloudsim.gp.provisioners.VideocardBwProvisioner;
import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;

public class VideocardSimple implements Videocard {
	
	private long id;
	private String type;
	private VGpuScheduler vgpuScheduler;
	private VideocardBwProvisioner pcieBwProvisioner;
	
	public VideocardSimple () {
		
	}
	
	@Override 
	public long getId () {
		return id;
	}
	
	@Override 
	public void setId (long id) {
		this.id = id;
	}
	
	@Override 
	public String getType () {
		return type;
	}
	
	@Override 
	public void setType (String type) {
		this.type = type;
	}
	
	@Override 
	public VGpuScheduler getVGpuScheduler () {
		return vgpuScheduler;
	}
	
	@Override 
	public Videocard setVGpuScheduler (VGpuScheduler vgpuScheduler) {
		
	}
	
	@Override 
	public VideocardBwProvisioner getPcieBwProvisioner () {
		return pcieBwProvisioner;
	}
	
	@Override public Videocard setPcieBwProvisioner (VideocardBwProvisioner pcieBwProvisioner) {
		
	}
}

