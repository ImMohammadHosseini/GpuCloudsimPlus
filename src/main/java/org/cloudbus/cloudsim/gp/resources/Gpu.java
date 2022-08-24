
package org.cloudbus.cloudsim.gp.resources;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.core.ChangeableId;
import org.cloudbus.cloudsim.resources.Pe; 
import org.cloudbus.cloudsim.vms.Vm;

import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisioner;


public interface Gpu extends ChangeableId {
	//, ResourceManageable
    //Logger LOGGER = LoggerFactory.getLogger(Vm.class.getSimpleName());
	
	Gpu NULL = new GpuNull ();
    
	List<Pe> getGpuCoreList ();
	
	GpuResourceProvisioner getGpuGddramProvisioner ();
	
	Gpu setGpuGddramProvisioner (GpuResourceProvisioner gpuGddramProvisioner);
	
	GpuResourceProvisioner getGpuBwProvisioner ();
	
	Gpu setGpuBwProvisioner (GpuResourceProvisioner gpuBwProvisioner);

}