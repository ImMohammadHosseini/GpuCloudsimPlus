package org.cloudbus.cloudsim.gp.provisioners;

//import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.resources.Gpu;

public interface GpuResourceProvisioner {
	
	GpuResourceProvisioner NULL = new GpuResourceProvisionerNull();
	
	boolean allocateResourceForVGpu (Gpu vgpu, long newTotalVmResourceCapacity);
	
	default boolean allocateResourceForVm (final Gpu vgpu, final double newTotalVmResource) {
        return allocateResourceForVm(vgpu, (long)newTotalVmResource);
    }
	
	long getAllocatedResourceForVGpu (Gpu vgpu);
	
	long getTotalAllocatedResource();
	
	long deallocateResourceForVGpu(Gpu vgpu);
	
	boolean isSuitableForVGpu (Gpu vgpu, long newVGpuTotalAllocatedResource);
	
	boolean isSuitableForVGpu(Gpu vgpu, Resource resource);
	
	ResourceManageable getPGpuResource();
	
	//void setResources(ResourceManageable pmResource, Function<Vm, ResourceManageable> vmResourceFunction);
	
	long getCapacity();
	
	long getAvailableResource();
}
