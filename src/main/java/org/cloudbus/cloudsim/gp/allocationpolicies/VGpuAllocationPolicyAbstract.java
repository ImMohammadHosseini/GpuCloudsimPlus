package org.cloudbus.cloudsim.gp.allocationpolicies;

import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.resources.GpuSuitability;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;


public abstract class VGpuAllocationPolicyAbstract implements VGpuAllocationPolicy {

	private BiFunction<VGpuAllocationPolicy, VGpu, Optional<Gpu>> findGpuForVGpuFunction;
    private Videocard videocard;
    private int gpuCountForParallelSearch;

    
    public VGpuAllocationPolicyAbstract () {
        this(null);
    }
    
    public VGpuAllocationPolicyAbstract (
    		final BiFunction<VGpuAllocationPolicy, VGpu, Optional<Gpu>> findGpuForVGpuFunction) {
        setVideocard(Videocard.NULL);
        setFindGpuForVGpuFunction(findGpuForVGpuFunction);
        this.gpuCountForParallelSearch = DEF_GPU_COUNT_PARALLEL_SEARCH;
    }
    
    @Override
    public final <T extends Gpu> List<T> getGpuList () {
        return videocard.getGpuList();
    }

    @Override
    public Videocard getVideocard () {
        return videocard;
    }
    
    @Override
    public void setVideocard (final Videocard videocard) {
        this.videocard = requireNonNull(videocard);
    }
    
    /*@Override
    public boolean scaleVmVertically(final VerticalVmScaling scaling) {
        if (scaling.isVmUnderloaded()) {
            return downScaleVmVertically(scaling);
        }

        if (scaling.isVmOverloaded()) {
            return upScaleVmVertically(scaling);
        }

        return false;
    }*/
    
    /*private boolean upScaleVmVertically(final VerticalVmScaling scaling) {
        return isRequestingCpuScaling(scaling) ? scaleVmPesUpOrDown(scaling) : upScaleVmNonCpuResource(scaling);
    }*/

    /*private boolean downScaleVmVertically(final VerticalVmScaling scaling) {
        return isRequestingCpuScaling(scaling) ? scaleVmPesUpOrDown(scaling) : downScaleVmNonCpuResource(scaling);
    }*/

    /*private boolean scaleVmPesUpOrDown(final VerticalVmScaling scaling) {*/
        

    /*private boolean isNotHostPesSuitableToUpScaleVm(final VerticalVmScaling scaling) {*/

    /*private boolean isRequestingCpuScaling(final VerticalVmScaling scaling) {
        return Processor.class.equals(scaling.getResourceClass());
    }*/

    /*private boolean upScaleVmNonCpuResource(final VerticalVmScaling scaling) {
        return scaling.allocateResourceForVm();
    }*/

    /*private boolean downScaleVmNonCpuResource(final VerticalVmScaling scaling) {*/

    
    @Override
    public GpuSuitability allocateGpuForVGpu (final VGpu vgpu) {
        if (getGpuList().isEmpty()) {
            LOGGER.error(
                "{}: {}: {} could not be allocated because there isn't any Gpu for Videocard {}",
                vgpu.getSimulation().clockStr(), getClass().getSimpleName(), vgpu,
                getVideocard().getHost().getId());
            return new GpuSuitability("Videocard has no host.");
        }

        if (vgpu.isCreated()) {
            return new GpuSuitability("VGpu is already created");
        }

        final Optional<Gpu> optionalGpu = findGpuForVGpu(vgpu);
        if (optionalGpu.filter(Gpu::isActive).isPresent()) {
            return allocateGpuForVGpu(vgpu, optionalGpu.get());
        }

        LOGGER.warn("{}: {}: No suitable gpu found for {} in {}", vgpu.getSimulation().clockStr(), 
        		getClass().getSimpleName(), vgpu, videocard);
        return new GpuSuitability("No suitable gpu found");
    }

    @Override
    public <T extends VGpu> List<T> allocateGpuForVGpu (final Collection<T> vgpuCollection) {
        requireNonNull(vgpuCollection, "The list of VGPUs to allocate a gpu to cannot be null");
        return vgpuCollection.stream().filter(
        		vgpu -> !allocateGpuForVGpu(vgpu).fully()).collect(toList());
    }

    @Override
    public GpuSuitability allocateGpuForVGpu (final VGpu vgpu, final Gpu gpu) {
        /*if(vm instanceof VmGroup vmGroup){
            return createVmsFromGroup(vmGroup, host);
        }*/

        return createVGpu(vgpu, gpu);
    }

    /*private HostSuitability createVGpusFromGroup(final VmGroup vmGroup, final Host host) {
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

    private GpuSuitability createVGpu (final VGpu vgpu, final Gpu gpu) {
        final GpuSuitability suitability = gpu.createVGpu(vgpu);
        if (suitability.fully()) {
            LOGGER.info(
                "{}: {}: {} has been allocated to {}",
                vgpu.getSimulation().clockStr(), getClass().getSimpleName(), vgpu, gpu);
        } else {
            LOGGER.error(
                "{}: {} Creation of {} on {} failed due to {}.",
                vgpu.getSimulation().clockStr(), getClass().getSimpleName(), vgpu, gpu, suitability);
        }

        return suitability;
    }

    @Override
    public void deallocateGpuForVGpu (final VGpu vgpu) {
        vgpu.getGpu().destroyVGpu(vgpu);
    }

    @Override
    public final void setFindGpuForVGpuFunction (
    		final BiFunction<VGpuAllocationPolicy, VGpu, Optional<Gpu>> findGpuForVGpuFunction) {
        this.findGpuForVGpuFunction = findGpuForVGpuFunction;
    }

    @Override
    public final Optional<Gpu> findGpuForVGpu (final VGpu vgpu) {
        final Optional<Gpu> optionalHost = findGpuForVGpuFunction == null ? 
        		defaultFindGpuForVGpu(vgpu) : findGpuForVGpuFunction.apply(this, vgpu);
        return optionalHost.map(host -> host.setActive(true));
    }

    protected abstract Optional<Gpu> defaultFindGpuForVGpu (VGpu vgpu);

    @Override
    public Map<VGpu, Gpu> getOptimizedAllocationMap (final List<? extends VGpu> vgpuList) {
    	//
        return Collections.emptyMap();
    }

    @Override
    public int getGpuCountForParallelSearch() {
        return gpuCountForParallelSearch;
    }

    @Override
    public void setGpuCountForParallelSearch (final int gpuCountForParallelSearch) {
        this.gpuCountForParallelSearch = gpuCountForParallelSearch;
    }

    @Override
    public boolean isVGpuMigrationSupported() {
        return false;
    }
}
