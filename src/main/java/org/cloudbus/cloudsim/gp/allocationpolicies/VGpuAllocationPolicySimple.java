package org.cloudbus.cloudsim.gp.allocationpolicies;

import java.util.stream.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiFunction;

import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.resources.Gpu;

import static java.util.Comparator.comparing;

public  class VGpuAllocationPolicySimple extends VGpuAllocationPolicyAbstract {

	public VGpuAllocationPolicySimple () {
        super();
    }

    public VGpuAllocationPolicySimple (
    		final BiFunction<VGpuAllocationPolicy, VGpu, Optional<Gpu>> findGpuForVGpuFunction) {
        super(findGpuForVGpuFunction);
    }
    
    @Override
    protected Optional<Gpu> defaultFindGpuForVGpu (final VGpu vgpu) {
        final Comparator<Gpu> comparator = comparing(Gpu::isActive).thenComparingLong(
        		Gpu::getFreeCoresNumber);

        final Stream<Gpu> gpuStream = isParallelGpuSearchEnabled() ? getGpuList().stream().parallel() : 
        	getGpuList().stream();
        return gpuStream.filter(gpu -> gpu.isSuitableForVGpu(vgpu)).max(comparator);
    }
}

