package org.cloudbus.cloudsim.gp.provisioners;

//import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.vms.vgpus.CustomVGpu;

import java.util.function.Function;

public interface GpuResourceProvisioner {
	
	GpuResourceProvisioner NULL = new GpuResourceProvisionerNull();
	
	boolean allocateResourceForVGpu (CustomVGpu vgpu, long newTotalVGpuResourceCapacity);
	
	default boolean allocateResourceForVGpu (final CustomVGpu vgpu, 
			final double newTotalVGpuResource) {
        return allocateResourceForVGpu(vgpu, (long)newTotalVGpuResource);
    }
	
	long getAllocatedResourceForVGpu (CustomVGpu vgpu);
	
	long getTotalAllocatedResource();
	
	long deallocateResourceForVGpu(CustomVGpu vgpu);
	
	boolean isSuitableForVGpu (CustomVGpu vgpu, long newVGpuTotalAllocatedResource);
	
	boolean isSuitableForVGpu(CustomVGpu vgpu, Resource resource);
	
	ResourceManageable getPGpuResource();
	
	void setResources(ResourceManageable pGpuResource, 
			Function<CustomVGpu, ResourceManageable> vGpuResourceFunction);
	
	long getCapacity();
	
	long getAvailableResource();
}
