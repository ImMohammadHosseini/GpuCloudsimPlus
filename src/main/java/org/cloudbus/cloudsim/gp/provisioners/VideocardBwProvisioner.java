package org.cloudbus.cloudsim.gp.provisioners;

import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.gp.resources.Gpu;

import java.util.function.Function;

public interface VideocardBwProvisioner {
	
	VideocardBwProvisioner NULL = new VideocardBwProvisionerNull();
	
	boolean allocateBwForGpu (Gpu gpu, long bw);
	
	default boolean allocateBwForGpu (final Gpu gpu, final double bw){
        return allocateBwForGpu (gpu, (long)bw);
    }
	
	long getAllocatedBwForGpu (Gpu gpu);
	
	long getTotalAllocatedBw ();
	
	long deallocateBwForGpu (Gpu gpu);
	
	boolean isSuitableForGpu (Gpu gpu, long newGpuTotalAllocatedBw);

    boolean isSuitableForGpu (Gpu gpu, Resource resource);

    ResourceManageable getVideocardBwResource ();

    void setResources (ResourceManageable videocardBwResource, 
    		Function<Gpu, ResourceManageable> gpuBwFunction);
    
    long getCapacity();

    long getAvailableResource();
}
