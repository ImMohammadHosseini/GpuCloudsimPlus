package org.cloudbus.cloudsim.gp.provisioners;

import java.util.function.Function;

import org.cloudbus.cloudsim.gp.vms.vgpus.CustomVGpu;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.ResourceManageable;

class GpuResourceProvisionerNull implements GpuResourceProvisioner {
	
	@Override public boolean allocateResourceForVGpu (CustomVGpu vgpu, long newTotalVmResourceCapacity) {
		return false;
	}
	
	@Override public long getAllocatedResourceForVGpu (CustomVGpu vgpu) {
		return 0;
	}
	
	@Override public long getTotalAllocatedResource() {
		return 0;
	}
	
	@Override public long deallocateResourceForVGpu(CustomVGpu vgpu) {
		return 0;
	}
	
	@Override public boolean isSuitableForVGpu (CustomVGpu vgpu, long newVGpuTotalAllocatedResource) {
		return false;
	}
	
	@Override public boolean isSuitableForVGpu(CustomVGpu vgpu, Resource resource) {
		return false;
	}
	
	@Override public ResourceManageable getPGpuResource() {
		return ResourceManageable.NULL;
	}
	
	@Override public void setResources(ResourceManageable pGpuResource, Function<CustomVGpu, ResourceManageable> vGpuResourceFunction) {
		
	}
	
	@Override public long getCapacity() {
		return 0;
	}
	
	@Override public long getAvailableResource() {
		return 0;
	}
}

