package org.cloudbus.cloudsim.gp.provisioners;

import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.resources.Gpu;

import java.util.Objects;
import java.util.function.Function;

public class VideocardBwProvisionerSimple implements VideocardBwProvisioner {
	
	private ResourceManageable videocardBwResource;
	
	private Function<Gpu, ResourceManageable> gpuBwFunction;
	
	public VideocardBwProvisionerSimple () {
		this(ResourceManageable.NULL, gpu -> ResourceManageable.NULL);
    }

    protected VideocardBwProvisionerSimple (final ResourceManageable videocardBwResource, 
    		final Function<Gpu, ResourceManageable> gpuBwFunction) {
    	setResources (videocardBwResource, gpuBwFunction);
    }
	
    @Override 
    public long getAllocatedBwForGpu (final Gpu gpu) { 
    	return gpuBwFunction.apply(gpu).getAllocatedResource();
    }

    @Override 
    public ResourceManageable getVideocardBwResource () {
        return videocardBwResource;
    }
    
    @Override 
    public void setResources (final ResourceManageable videocardBwResource, 
    		Function<Gpu, ResourceManageable> gpuBwFunction) {
    	
    	this.videocardBwResource = Objects.requireNonNull(videocardBwResource);
        this.gpuBwFunction = Objects.requireNonNull(gpuBwFunction);
    }
	
    @Override 
    public long getCapacity() { 
    	return videocardBwResource.getCapacity (); 
    }
    
    @Override 
    public long getTotalAllocatedBw () { 
    	return videocardBwResource.getAllocatedResource ();
    }
    
    @Override 
    public long getAvailableResource() { 
    	return videocardBwResource.getAvailableResource (); }

    @Override 
	public boolean allocateBwForGpu (final Gpu gpu, final long bw) { 
    	Objects.requireNonNull(gpu);

        if (!isSuitableForGpu(gpu, bw)) {
            return false;
        }

        final ResourceManageable gpuResource = getGpuBwFunction().apply(gpu);
        final long prevGpuBwAllocation = gpuResource.getAllocatedResource();
        if (prevGpuBwAllocation > 0) 
            deallocateBwForGpu(gpu);

        if(!gpuResource.setCapacity(bw)){
            return false;
        }

        getVideocardBwResource().allocateResource(bw);
        gpuResource.setCapacity(bw);
        gpuResource.setAllocatedResource(bw);
        return true;
	}
    
    @Override
    public boolean allocateBwForGpu (final Gpu gpu, final double newTotalVmResource) {
        return allocateBwForGpu (gpu, (long)newTotalVmResource);
    }
    
    @Override 
    public boolean isSuitableForGpu (final Gpu gpu, final long newGpuTotalAllocatedBw) {
    	final long currentAllocatedBw = getAllocatedBwForGpu (gpu);
        final long allocationDifference = newGpuTotalAllocatedBw - currentAllocatedBw;
        return getVideocardBwResource().getAvailableResource() >=  allocationDifference;
    }
    
    @Override 
    public boolean isSuitableForGpu (final Gpu gpu, final Resource resource) { 
    	return isSuitableForGpu(gpu, resource.getCapacity()); 
    }
    
    @Override 
    public long deallocateBwForGpu (final Gpu gpu) { 
    	final ResourceManageable gpuResource = getGpuBwFunction().apply(gpu);
        final long gpuAllocatedResource = gpuResource.getAllocatedResource();

        gpuResource.deallocateAllResources();

        getVideocardBwResource().deallocateResource(gpuAllocatedResource);
        return gpuAllocatedResource;
    }
    protected Function<Gpu, ResourceManageable> getGpuBwFunction() {
        return gpuBwFunction;
    }
    
    
}
