package org.cloudbus.cloudsim.gp.allocationpolicies;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyAbstract;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;
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
			final BiFunction<GpuVmAllocationPolicy, GpuVm, Optional<GpuHost>> 
					findGpuHostForGpuVmFunction) {
        setGpuDatacenter(GpuDatacenter.NULL);
        setFindGpuHostForGpuVmFunction(findGpuHostForGpuVmFunction);
        this.gpuHostCountForParallelSearch = DEF_GPUHOST_COUNT_PARALLEL_SEARCH;
    }

    @Override
    public final <T extends GpuHost> List<T> getGpuHostList () {
        return gpudatacenter.getHostList();
    }

    @Override
    public GpuDatacenter getGpuDatacenter () {
        return gpudatacenter;
    }

    @Override
    public void setGpuDatacenter(final GpuDatacenter datacenter) {
        this.gpudatacenter = requireNonNull(datacenter);
    }
    
    @Override
    public boolean scaleGpuVmVertically (final VerticalVmScaling scaling) {
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
    public HostSuitability allocateGpuHostForGpuVm (final GpuVm vm) {
        if (getHostList().isEmpty()) {
            LOGGER.error(
                "{}: {}: {} could not be allocated because there isn't any GpuHost for GpuDatacenter {}",
                vm.getSimulation().clockStr(), getClass().getSimpleName(), vm, 
                getGpuDatacenter().getId());
            return new HostSuitability("GpuDatacenter has no Gpuhost.");
        }

        if (vm.isCreated()) {
            return new HostSuitability("VM is already created");
        }

        final var optionalGpuHost = findGpuHostForGpuVm(vm);
        if (optionalGpuHost.filter(GpuHost::isActive).isPresent()) {
            return allocateGpuHostForGpuVm(vm, optionalGpuHost.get());
        }

        LOGGER.warn("{}: {}: No suitable Gpuhost found for {} in {}", vm.getSimulation().clockStr(), getClass().getSimpleName(), vm, datacenter);
        return new HostSuitability("No suitable host found");
    }

    @Override
    public <T extends GpuVm> List<T> allocateGpuHostForGpuVm (final Collection<T> gpuvmCollection) {
        requireNonNull(gpuvmCollection, "The list of GpuVMs to allocate a Gpuhost to cannot be null");
        return gpuvmCollection.stream().filter(gpuvm -> !allocateGpuHostForGpuVm(
        		gpuvm).fully()).collect(toList());
    }

    @Override
    public HostSuitability allocateGpuHostForGpuVm (final GpuVm vm, final GpuHost host) {
        /*if(vm instanceof VmGroup vmGroup){
            return createVmsFromGroup(vmGroup, host);
        }*/

        return createGpuVm(vm, host);
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

    private HostSuitability createGpuVm (final GpuVm vm, final GpuHost host) {
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
    public void deallocateGpuHostForGpuVm (final GpuVm vm) {
        vm.getHost().destroyVm(vm);
    }

    @Override
    public final void setFindGpuHostForGpuVmFunction (
    		final BiFunction<GpuVmAllocationPolicy, GpuVm, Optional<GpuHost>> findGpuHostForGpuVmFunction) {
        this.findGpuHostForGpuVmFunction = findGpuHostForGpuVmFunction;
    }

    @Override
    public final Optional<GpuHost> findGpuHostForGpuVm(final GpuVm vm) {
        final var optionalHost = findGpuHostForGpuVmFunction == null ? 
        		defaultFindGpuHostForGpuVm (vm) : findGpuHostForGpuVmFunction.apply (this, vm);
        //If the selected Host is not active, activate it (if it's already active, setActive has no effect)
        return optionalHost.map(gpuHost -> gpuHost.setActive(true));
    }

    protected abstract Optional<GpuHost> defaultFindGpuHostForGpuVm (GpuVm vm);

    @Override
    public Map<GpuVm, GpuHost> getOptimizedAllocationMap (final List<? extends GpuVm> gpuvmList) {
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
    public int getGpuHostCountForParallelSearch () {
        return gpuHostCountForParallelSearch;
    }

    @Override
    public void setGpuHostCountForParallelSearch (final int hostCountForParallelSearch) {
        this.gpuHostCountForParallelSearch = hostCountForParallelSearch;
    }

    @Override
    public boolean isGpuVmMigrationSupported () {
        return false;
    }
}
