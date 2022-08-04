package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisioner;

import org.cloudbus.cloudsim.resources.Pe; 

import java.util.List;
import java.util.Collections;


final class GpuNull implements Gpu {
	
	@Override public void setId(long id) {/**/}
	@Override public long getId() {
        return -1;
    }
	
	@Override public List<Pe> getGpuPeList() {
        return Collections.emptyList();
    }
	
	@Override public Gpu setGpuGddramProvisioner (GpuResourceProvisioner gpuGddramProvisioner) {
        return Gpu.NULL;
	}
	
	@Override public GpuResourceProvisioner getGpuGddramProvisioner () {
        return GpuResourceProvisioner.NULL;
    }
        
    @Override public Gpu setGpuBwProvisioner (GpuResourceProvisioner gpuBwProvisioner) {
    	return Gpu.NULL;
    }
    
    @Override public GpuResourceProvisioner getGpuBwProvisioner () {
        return GpuResourceProvisioner.NULL;
    }
}
