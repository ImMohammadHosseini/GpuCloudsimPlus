package org.cloudbus.cloudsim.gp.provisioners;

import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

import java.util.function.Function;
import java.util.Objects;


public abstract class GpuResourceProvisionerAbstract implements GpuResourceProvisioner {
	
	private ResourceManageable pGpuResource;
	private Function<CustomVGpu, ResourceManageable> vGpuResourceFunction;
	
	protected GpuResourceProvisionerAbstract() {
        this(ResourceManageable.NULL, vgpu -> ResourceManageable.NULL);
    }

    public GpuResourceProvisionerAbstract(final ResourceManageable pGpuResource, 
    		final Function<CustomVGpu, ResourceManageable> vGpuResourceFunction) {
        setResources(pGpuResource, vGpuResourceFunction);
    }
    
    
	@Override
    public long getAllocatedResourceForVGpu(final CustomVGpu vGpu) {
        return vGpuResourceFunction.apply(vGpu).getAllocatedResource();
    }
	
	@Override
    public ResourceManageable getPGpuResource() {
        return pGpuResource;
    }

    @Override
    public final void setResources(final ResourceManageable pGpuResource, 
    		final Function<CustomVGpu, ResourceManageable> vGpuResourceFunction) {
        this.pGpuResource = Objects.requireNonNull(pGpuResource);
        this.vGpuResourceFunction = Objects.requireNonNull(vGpuResourceFunction);
    }

    @Override
    public long getCapacity() {
        return pGpuResource.getCapacity();
    }

    @Override
    public long getTotalAllocatedResource() {
        return pGpuResource.getAllocatedResource();
    }

    @Override
    public long getAvailableResource() {
        return pGpuResource.getAvailableResource();
    }
    
    protected Function<CustomVGpu, ResourceManageable> getVGpuResourceFunction() {
        return vGpuResourceFunction;
    }
}
