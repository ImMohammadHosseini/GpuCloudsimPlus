package org.cloudbus.cloudsim.gp.provisioners;

import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.resources.Gpu;

import java.util.function.Function;

public class VideocardBwProvisionerSimple implements VideocardBwProvisioner {
	
	private ResourceManageable videocardBwResource;
	
	private Function<Gpu, ResourceManageable> gpuBwFunction;
	
	

}
