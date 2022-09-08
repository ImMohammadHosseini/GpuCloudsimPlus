package org.cloudbus.cloudsim.gp.provisioners;

import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

import java.util.Objects;
import java.util.function.Function;


public class GpuResourceProvisionerSimple extends GpuResourceProvisionerAbstract {
	
	public GpuResourceProvisionerSimple() {
        super(ResourceManageable.NULL, vgpu -> ResourceManageable.NULL);
    }

    protected GpuResourceProvisionerSimple(final ResourceManageable resource, 
    		final Function<VGpu, ResourceManageable> vGpuResourceFunction) {
        super(resource, vGpuResourceFunction);
    }
    
    @Override
    public boolean allocateResourceForVGpu (final VGpu vgpu, 
    		final long newTotalVGpuResourceCapacity) {
    	
    	Objects.requireNonNull(vgpu);

        if (!isSuitableForVGpu (vgpu, newTotalVGpuResourceCapacity)) {
            return false;
        }

        
        final ResourceManageable vGpuResource = getVGpuResourceFunction().apply(vgpu);
        final long prevVGpuResourceAllocation = vGpuResource.getAllocatedResource();
        if (prevVGpuResourceAllocation > 0) {
            deallocateResourceForVGpu (vgpu);
        }

        if(!getPGpuResource().isSubClassOf(Pe.class) && !vGpuResource.setCapacity(newTotalVGpuResourceCapacity)){
            return false;
        }

        getPGpuResource().allocateResource(newTotalVGpuResourceCapacity);
        vGpuResource.setCapacity(newTotalVGpuResourceCapacity);
        vGpuResource.setAllocatedResource(newTotalVGpuResourceCapacity);
        return true;
    }
    
    @Override
    public boolean allocateResourceForVGpu (final VGpu vgpu, 
    		final double newTotalVGpuResourceCapacity) {
        return allocateResourceForVGpu(vgpu, (long)newTotalVGpuResourceCapacity);
    }
    
    @Override
    public long deallocateResourceForVGpu (final VGpu vgpu) {
        final ResourceManageable vGpuResource = getVGpuResourceFunction().apply(vgpu);
        final long vGpuAllocatedResource = vGpuResource.getAllocatedResource();

        vGpuResource.deallocateAllResources();

        getPGpuResource().deallocateResource(vGpuAllocatedResource);
        return vGpuAllocatedResource;
    }

    @Override
    public boolean isSuitableForVGpu (VGpu vgpu, long newVGpuTotalAllocatedResource) {
        final long currentAllocatedResource = getAllocatedResourceForVGpu (vgpu);
        final long allocationDifference = newVGpuTotalAllocatedResource - currentAllocatedResource;
        return getPGpuResource().getAvailableResource() >=  allocationDifference;
    }

    @Override
    public boolean isSuitableForVGpu (final VGpu vgpu, final Resource resource) {
        return isSuitableForVGpu(vgpu, resource.getCapacity());
    }
}
