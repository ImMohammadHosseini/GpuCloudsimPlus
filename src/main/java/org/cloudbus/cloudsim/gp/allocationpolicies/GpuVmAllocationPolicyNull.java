package org.cloudbus.cloudsim.gp.allocationpolicies;

import java.util.*;
import java.util.function.BiFunction;

import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenter;
//import org.cloudbus.cloudsim.gp.hosts.GpuHostSuitability;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;
import org.cloudbus.cloudsim.gp.vms.GpuVm;

import org.cloudbus.cloudsim.hosts.HostSuitability;
import org.cloudsimplus.autoscaling.VerticalVmScaling;

final class GpuVmAllocationPolicyNull implements GpuVmAllocationPolicy {

	@Override public GpuDatacenter getGpuDatacenter () { return GpuDatacenter.NULL; }

	@Override public void setGpuDatacenter (GpuDatacenter datacenter) { /**/ }
	@Override public HostSuitability allocateGpuHostForGpuVm (GpuVm vm) {
		return HostSuitability.NULL;
	}
	@Override public HostSuitability allocateGpuHostForGpuVm (GpuVm vm, GpuHost host) {
		return HostSuitability.NULL;
	}
	@Override public <T extends GpuVm> List<T> allocateGpuHostForGpuVm (
			Collection<T> gpuvmCollection) {
		return Collections.emptyList();
	}
	@Override public boolean scaleGpuVmVertically (VerticalVmScaling scaling) { return false; }
	@Override public void deallocateGpuHostForGpuVm (GpuVm vm) { /**/ }
	@Override public void setFindGpuHostForGpuVmFunction (
			BiFunction<GpuVmAllocationPolicy, GpuVm, Optional<GpuHost>> findGpuHostForGpuVmFunction) { 
		/**/ 
	}
	@Override public List<GpuHost> getGpuHostList () { return Collections.emptyList(); }
	@Override public Map<GpuVm, GpuHost> getOptimizedAllocationMap (List<? extends GpuVm> gpuvmList) { 
		return Collections.emptyMap(); }
	@Override public Optional<GpuHost> findGpuHostForGpuVm (GpuVm vm) { return Optional.empty(); }
	@Override public boolean isGpuVmMigrationSupported () { return false; }
	@Override public int getGpuHostCountForParallelSearch () { return 0; }
	@Override public void setGpuHostCountForParallelSearch (int gpuHostCountForParallelSearch) { /**/ }
	
}