package org.cloudbus.cloudsim.gp.allocationpolicies;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.Processor;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.hosts.HostSuitability;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyAbstract;

import org.cloudsimplus.autoscaling.VerticalVmScaling;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;
//import org.cloudbus.cloudsim.gp.hosts.GpuHostSuitability;
import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenter;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public abstract class GpuVmAllocationPolicyAbstract implements GpuVmAllocationPolicy {
	
	private BiFunction<GpuVmAllocationPolicy, GpuVm, Optional<GpuHost>> 
				findGpuHostForGpuVmFunction;

    private GpuDatacenter gpudatacenter;

    private int gpuHostCountForParallelSearch;
    
	public GpuVmAllocationPolicyAbstract () {
		this(null);
	}
	
	public GpuVmAllocationPolicyAbstract (
			final BiFunction<VmAllocationPolicy, Vm, Optional<Host>> 
					findGpuHostForGpuVmFunction) {
        setDatacenter(GpuDatacenter.NULL);
        setFindHostForVmFunction(findGpuHostForGpuVmFunction);
        this.gpuHostCountForParallelSearch = DEF_GPUHOST_COUNT_PARALLEL_SEARCH;
    }

    @Override
    public final <T extends Host> List<T> getHostList () {
        return gpudatacenter.getHostList();
    }

    @Override
    public GpuDatacenter getDatacenter () {
        return gpudatacenter;
    }

    @Override
    public void setDatacenter(final Datacenter datacenter) {
        this.gpudatacenter = requireNonNull((GpuDatacenter)datacenter);
    }
    
    @Override
    public boolean scaleVmVertically (final VerticalVmScaling scaling) {
        if (scaling.isVmUnderloaded()) {
            return downScaleVmVertically(scaling);
        }

        if (scaling.isVmOverloaded()) {
            return upScaleVmVertically(scaling);
        }

        return false;
    }
    
    private boolean upScaleVmVertically(final VerticalVmScaling scaling) {
        return isRequestingCpuScaling(scaling) ? scaleVmPesUpOrDown(scaling) : upScaleVmNonCpuResource(scaling);
    }

    private boolean downScaleVmVertically(final VerticalVmScaling scaling) {
        return isRequestingCpuScaling(scaling) ? scaleVmPesUpOrDown(scaling) : downScaleVmNonCpuResource(scaling);
    }

    private boolean scaleVmPesUpOrDown(final VerticalVmScaling scaling) {
        final double pesNumberForScaling = scaling.getResourceAmountToScale();
        if (pesNumberForScaling == 0) {
            return false;
        }

        final boolean isVmUnderloaded = scaling.isVmUnderloaded();
        //Avoids trying to downscale the number of vPEs to zero
        if(isVmUnderloaded && scaling.getVm().getNumberOfPes() == pesNumberForScaling) {
            scaling.logDownscaleToZeroNotAllowed();
            return false;
        }

        if (scaling.isVmOverloaded() && isNotHostPesSuitableToUpScaleVm(scaling)) {
            scaling.logResourceUnavailable();
            return false;
        }

        final Vm vm = scaling.getVm();
        vm.getHost().getVmScheduler().deallocatePesFromVm(vm);
        final int signal = isVmUnderloaded ? -1 : 1;
        //Removes or adds some capacity from/to the resource, respectively if the VM is under or overloaded
        vm.getProcessor().sumCapacity((long) pesNumberForScaling * signal);

        vm.getHost().getVmScheduler().allocatePesForVm(vm);
        return true;
    }

    private boolean isNotHostPesSuitableToUpScaleVm(final VerticalVmScaling scaling) {
        final Vm vm = scaling.getVm();
        final long pesCountForScaling = (long)scaling.getResourceAmountToScale();
        final MipsShare additionalVmMips = new MipsShare(pesCountForScaling, vm.getMips());
        return !vm.getHost().getVmScheduler().isSuitableForVm(vm, additionalVmMips);
    }
    
    private boolean isRequestingCpuScaling(final VerticalVmScaling scaling) {
        return Processor.class.equals(scaling.getResourceClass());
    }
    
    private boolean upScaleVmNonCpuResource(final VerticalVmScaling scaling) {
        return scaling.allocateResourceForVm();
    }
    
    private boolean downScaleVmNonCpuResource(final VerticalVmScaling scaling) {
        final var resourceManageableClass = scaling.getResourceClass();
        final var vmResource = scaling.getVm().getResource(resourceManageableClass);
        final double amountToDeallocate = scaling.getResourceAmountToScale();
        final var resourceProvisioner = scaling.getVm().getHost().getProvisioner(resourceManageableClass);
        final double newTotalVmResource = vmResource.getCapacity() - amountToDeallocate;
        if (resourceProvisioner.allocateResourceForVm(scaling.getVm(), newTotalVmResource)) {
            LOGGER.info(
                "{}: {}: {} {} deallocated from {}: new capacity is {}. Current resource usage is {}%",
                scaling.getVm().getSimulation().clockStr(),
                scaling.getClass().getSimpleName(),
                (long) amountToDeallocate, resourceManageableClass.getSimpleName(),
                scaling.getVm(), vmResource.getCapacity(),
                vmResource.getPercentUtilization() * 100);
            return true;
        }

        LOGGER.error(
            "{}: {}: {} requested to reduce {} capacity by {} but an unexpected error occurred and the resource was not resized",
            scaling.getVm().getSimulation().clockStr(),
            scaling.getClass().getSimpleName(),
            scaling.getVm(),
            resourceManageableClass.getSimpleName(), (long) amountToDeallocate);
        return false;

    }

    @Override
    public HostSuitability allocateHostForVm (final Vm vm) {
        if (getHostList().isEmpty()) {
            LOGGER.error(
                "{}: {}: {} could not be allocated because there isn't any GpuHost for "
                + "GpuDatacenter {}",
                vm.getSimulation().clockStr(), getClass().getSimpleName(), vm, 
                getDatacenter().getId());
            return new HostSuitability("GpuDatacenter has no Gpuhost.");
        }

        if (vm.isCreated()) {
            return new HostSuitability("GpuVM is already created");
        }

        final var optionalGpuHost = findHostForVm(vm);
        if (optionalGpuHost.filter(Host::isActive).isPresent()) {
            return allocateHostForVm(vm, optionalGpuHost.get());
        }

        LOGGER.warn("{}: {}: No suitable Gpuhost found for {} in {}", vm.getSimulation().clockStr(), 
        		getClass().getSimpleName(), vm, gpudatacenter);
        return new HostSuitability("No suitable Gpuhost found");
    }

    @Override
    public <T extends Vm> List<T> allocateHostForVm (final Collection<T> gpuvmCollection) {
        requireNonNull(gpuvmCollection, "The list of GpuVMs to allocate a Gpuhost to cannot be null");
        return gpuvmCollection.stream().filter(gpuvm -> !allocateHostForVm(
        		gpuvm).fully()).collect(toList());
    }

    @Override
    public HostSuitability allocateHostForVm (final Vm vm, final Host host) {
        /*if(vm instanceof VmGroup vmGroup){
            return createVmsFromGroup(vmGroup, host);
        }*/

        return createVm((GpuVm)vm, (GpuHost)host);
    }

    /*private HostSuitability createGpuVmsFromGroup (final VmGroup vmGroup, final Host host) {
        int createdVms = 0;
        final var hostSuitabilityForVmGroup = new HostSuitability();
        for (final Vm vm : vmGroup.getVmList()) {
            final var hostSuitability = createVm(vm, host);
            hostSuitabilityForVmGroup.setSuitability(hostSuitability);
            createdVms += Conversion.boolToInt(hostSuitability.fully());
        }

        vmGroup.setCreated(createdVms > 0);
        if(vmGroup.isCreated()) {
            vmGroup.setHost(host);
        }

        return hostSuitabilityForVmGroup;
    }*/

    private HostSuitability createVm (final GpuVm vm, final GpuHost host) {
        final var suitability = host.createVm(vm);
        if (suitability.fully()) {
            LOGGER.info(
                "{}: {}: {} has been allocated to {}",
                vm.getSimulation().clockStr(), getClass().getSimpleName(), vm, host);
        } else {
            LOGGER.error(
                "{}: {} Creation of {} on {} failed due to {}.",
                vm.getSimulation().clockStr(), getClass().getSimpleName(), vm, host, suitability);
        }

        return suitability;
    }

    @Override
    public void deallocateHostForVm (final Vm vm) {
        vm.getHost().destroyVm(vm);
    }

    @Override
    public final void setFindHostForVmFunction (
    		final BiFunction<VmAllocationPolicy, Vm, Optional<Host>> findGpuHostForGpuVmFunction) {
        this.findGpuHostForGpuVmFunction = (BiFunction)findGpuHostForGpuVmFunction;
    }

    @Override
    public final Optional<Host> findHostForVm (final Vm vm) {
        final var optionalHost = findGpuHostForGpuVmFunction == null ? 
        		defaultFindGpuHostForGpuVm ((GpuVm)vm) : 
        			findGpuHostForGpuVmFunction.apply (this, (GpuVm)vm);
        //optionalHost = Optional.of((GpuHost)optionalHost.get().setActive(true));
        return optionalHost.map(gpuHost -> gpuHost.setActive(true));
        //return optionalHost;
    }

    protected abstract Optional<Host> defaultFindGpuHostForGpuVm (GpuVm vm);

    @Override
    public Map<Vm, Host> getOptimizedAllocationMap (final List<? extends Vm> gpuvmList) {
        /*
         * This method implementation doesn't perform any
         * VM placement optimization and, in fact, has no effect.
         * Classes implementing the {@link VmAllocationPolicyMigration}
         * provide actual implementations for this method that can be overridden
         * by subclasses.
         */
        return Collections.emptyMap();
    }

    @Override
    public int getHostCountForParallelSearch () {
        return gpuHostCountForParallelSearch;
    }

    @Override
    public void setHostCountForParallelSearch (final int hostCountForParallelSearch) {
        this.gpuHostCountForParallelSearch = hostCountForParallelSearch;
    }

    @Override
    public boolean isVmMigrationSupported () {
        return false;
    }
}
