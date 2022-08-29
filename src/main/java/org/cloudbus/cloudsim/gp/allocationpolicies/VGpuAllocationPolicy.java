package org.cloudbus.cloudsim.gp.allocationpolicies;

import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.resources.GpuSuitability;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface VGpuAllocationPolicy {

	Logger LOGGER = LoggerFactory.getLogger(VGpuAllocationPolicy.class.getSimpleName());

	VGpuAllocationPolicy NULL = new VGpuAllocationPolicyNull();
	
	int DEF_GPU_COUNT_PARALLEL_SEARCH = 2_000;
	
	Videocard getVideocard ();

    void setVideocard(Videocard videocard);

    GpuSuitability allocateGpuForVGpu (CustomVGpu vgpu);

    GpuSuitability allocateGpuForVGpu (CustomVGpu vgpu, Gpu gpu);

    <T extends CustomVGpu> List<T> allocateGpuForVGpu (Collection<T> vgpuCollection);

    /**
     * Try to scale some Vm's resource vertically up or down, respectively if:
     * <ul>
     *     <li>the Vm is overloaded and the Host where the Vm is placed has enough capacity</li>
     *     <li>the Vm is underloaded</li>
     * </ul>
     *
     * The resource to be scaled is defined by the given {@link VerticalVmScaling} object.
     *
     * @param scaling the {@link VerticalVmScaling} object with information of which resource
     *                is being requested to be scaled
     * @return true if the requested resource was scaled, false otherwise
     */
    //boolean scaleVmVertically (VerticalVmScaling scaling);

    void deallocateGpuForVGpu (CustomVGpu vgpu);

    void setFindGpuForVGpuFunction (
    		BiFunction<VGpuAllocationPolicy, CustomVGpu, Optional<Gpu>> findGpuForVGpuFunction);

     <T extends Gpu> List<T> getGpuList ();

    Map<CustomVGpu, Gpu> getOptimizedAllocationMap (List<? extends CustomVGpu> vgpuList);

    Optional<Gpu> findGpuForVGpu (CustomVGpu vgpu);

    boolean isVGpuMigrationSupported ();

    default boolean isParallelGpuSearchEnabled () {
        return getGpuList().size() >= getGpuCountForParallelSearch();
    }
    
    int getGpuCountForParallelSearch ();

    void setGpuCountForParallelSearch (int gpuCountForParallelSearch);
}