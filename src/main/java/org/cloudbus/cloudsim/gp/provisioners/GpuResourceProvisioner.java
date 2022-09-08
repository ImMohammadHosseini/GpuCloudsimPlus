package org.cloudbus.cloudsim.gp.provisioners;

//import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

import java.util.function.Function;

public interface GpuResourceProvisioner {
	
	GpuResourceProvisioner NULL = new GpuResourceProvisionerNull();
	
	boolean allocateResourceForVGpu (VGpu vgpu, long newTotalVGpuResourceCapacity);
	
	default boolean allocateResourceForVGpu (final VGpu vgpu, 
			final double newTotalVGpuResource) {
        return allocateResourceForVGpu(vgpu, (long)newTotalVGpuResource);
    }
	
	long getAllocatedResourceForVGpu (VGpu vgpu);
	
	long getTotalAllocatedResource();
	
	long deallocateResourceForVGpu(VGpu vgpu);
	
	boolean isSuitableForVGpu (VGpu vgpu, long newVGpuTotalAllocatedResource);
	
	boolean isSuitableForVGpu(VGpu vgpu, Resource resource);
	
	ResourceManageable getPGpuResource();
	
	void setResources(ResourceManageable pGpuResource, 
			Function<VGpu, ResourceManageable> vGpuResourceFunction);
	
	long getCapacity();
	
	long getAvailableResource();
}
