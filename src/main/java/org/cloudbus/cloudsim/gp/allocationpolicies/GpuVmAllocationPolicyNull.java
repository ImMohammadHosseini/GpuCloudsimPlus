package org.cloudbus.cloudsim.gp.allocationpolicies;

import java.util.*;
import java.util.function.BiFunction;

import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenter;
//import org.cloudbus.cloudsim.gp.hosts.GpuHostSuitability;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSuitability;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudsimplus.autoscaling.VerticalVmScaling;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;

final class GpuVmAllocationPolicyNull implements GpuVmAllocationPolicy {

	@Override public GpuDatacenter getDatacenter () { return GpuDatacenter.NULL; }

	@Override public void setDatacenter (Datacenter datacenter) { /**/ }
	@Override public HostSuitability allocateHostForVm (Vm vm) {
		return HostSuitability.NULL;
	}
	@Override public HostSuitability allocateHostForVm (Vm vm, Host host) {
		return HostSuitability.NULL;
	}
	@Override public <T extends Vm> List<T> allocateHostForVm (Collection<T> gpuvmCollection) {
		return Collections.emptyList();
	}
	@Override public boolean scaleVmVertically (VerticalVmScaling scaling) { return false; }
	@Override public void deallocateHostForVm (Vm vm) { /**/ }
	@Override public void setFindHostForVmFunction (
			BiFunction<VmAllocationPolicy, Vm, Optional<Host>> findGpuHostForGpuVmFunction) { 
		/**/ 
	}
	@Override public List<GpuHost> getHostList () { return Collections.emptyList(); }
	@Override public Map<Vm, Host> getOptimizedAllocationMap (List<? extends Vm> gpuvmList) { 
		return Collections.emptyMap(); }
	@Override public Optional<Host> findHostForVm (Vm vm) { return Optional.empty(); }
	@Override public boolean isVmMigrationSupported () { return false; }
	@Override public int getHostCountForParallelSearch () { return 0; }
	@Override public void setHostCountForParallelSearch (int gpuHostCountForParallelSearch) { /**/ }
	
}