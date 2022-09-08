package org.cloudbus.cloudsim.gp.allocationpolicies;

import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.resources.GpuSuitability;
import org.cloudbus.cloudsim.gp.videocards.Videocard;

public final class VGpuAllocationPolicyNull implements VGpuAllocationPolicy {

	@Override public Videocard getVideocard () { return Videocard.NULL; }
	@Override public void setVideocard (Videocard videocard) { /**/ }
	@Override public GpuSuitability allocateGpuForVGpu (VGpu vgpu) { return GpuSuitability.NULL; }
	@Override public GpuSuitability allocateGpuForVGpu (VGpu vgpu, Gpu gpu) {
		return GpuSuitability.NULL;
	}
	@Override public <T extends VGpu> List<T> allocateGpuForVGpu (Collection<T> vgpuCollection) {
		return Collections.emptyList();
	}
	//@Override public boolean scaleVmVertically (VerticalVmScaling scaling) { return false; }
	@Override public void deallocateGpuForVGpu (VGpu vgpu) { /**/ }
	@Override public void setFindGpuForVGpuFunction (
			BiFunction<VGpuAllocationPolicy, VGpu, Optional<Gpu>> findGpuForVGpuFunction) { /**/ }
	
	@Override public List<Gpu> getGpuList () { return Collections.emptyList(); }
	@Override public Map<VGpu, Gpu> getOptimizedAllocationMap (
			List<? extends VGpu> vgpuList) { return Collections.emptyMap(); }

	@Override public Optional<Gpu> findGpuForVGpu (VGpu vgpu) { return Optional.empty(); }
	@Override public boolean isVGpuMigrationSupported () { return false; }
	@Override public int getGpuCountForParallelSearch () { return 0; }
	@Override public void setGpuCountForParallelSearch (int gpuCountForParallelSearch) { /**/ }

}
