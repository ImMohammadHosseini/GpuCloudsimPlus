package org.cloudbus.cloudsim.gp.allocationpolicies;

import org.cloudbus.cloudsim.gp.hosts.GpuHost;
import org.cloudbus.cloudsim.gp.vms.GpuVm;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.Comparator.comparing;

public abstract class GpuVmAllocationPolicySimple extends GpuVmAllocationPolicyAbstract{
	
	public GpuVmAllocationPolicySimple () {
        super();
    }

    public GpuVmAllocationPolicySimple (
    		final BiFunction<GpuVmAllocationPolicy, GpuVm, Optional<GpuHost>> findGpuHostForGpuVmFunction) {
        super(findGpuHostForGpuVmFunction);
    }

    @Override
    protected Optional<GpuHost> defaultFindGpuHostForGpuVm (final GpuVm vm) {
        final Comparator<GpuHost> comparator = comparing(GpuHost::isActive).thenComparingLong(
        		GpuHost::getFreePesNumber);

        final var hostStream = isParallelGpuHostSearchEnabled() ? 
        		getGpuHostList().stream().parallel() : getGpuHostList().stream();
        return hostStream.filter(host -> host.isSuitableForVm(vm)).max(comparator);
    }
}

