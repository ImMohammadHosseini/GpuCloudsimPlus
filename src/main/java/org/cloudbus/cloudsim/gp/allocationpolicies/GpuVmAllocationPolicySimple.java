package org.cloudbus.cloudsim.gp.allocationpolicies;

//import org.cloudbus.cloudsim.gp.hosts.GpuHost;
import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.Comparator.comparing;

public class GpuVmAllocationPolicySimple extends GpuVmAllocationPolicyAbstract{
	
	public GpuVmAllocationPolicySimple () {
        super();
    }

    public GpuVmAllocationPolicySimple (
    		final BiFunction<VmAllocationPolicy, Vm, Optional<Host>> findGpuHostForGpuVmFunction) {
        super(findGpuHostForGpuVmFunction);
    }

    @Override
    protected Optional<Host> defaultFindGpuHostForGpuVm (final GpuVm vm) {
        final Comparator<Host> comparator = comparing(Host::isActive).thenComparingLong(
        		Host::getFreePesNumber);

        final var hostStream = isParallelHostSearchEnabled() ? 
        		getHostList().stream().parallel() : getHostList().stream();
        return hostStream.filter(host -> host.isSuitableForVm(vm)).max(comparator);
    }
}

