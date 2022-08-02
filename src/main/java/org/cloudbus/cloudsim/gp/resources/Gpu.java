
package org.cloudbus.cloudsim.gp.resources;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.core.ChangeableId;

import org.cloudbus.cloudsim.vms.Vm;


public interface Gpu extends ChangeableId, ResourceManageable{
	
    //Logger LOGGER = LoggerFactory.getLogger(Vm.class.getSimpleName());
	
	Gpu NULL = new GpuNull();
    
}