package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.gp.resources.CustomVGpuSimple;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.gp.resources.GpuSimple;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.resources.Pe;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public abstract class VGpuSchedulerAbstract implements VGpuScheduler {
	
	private Gpu gpu;
    private final double vgpuMigrationGpuOverhead;//vgpuMigrationCpuOverhead
    
    public VGpuSchedulerAbstract (final double vgpuMigrationGpuOverhead) {
        if(vgpuMigrationGpuOverhead < 0 || vgpuMigrationGpuOverhead >= 1){
            throw new IllegalArgumentException("vgpuMigrationGpuOverhead must be a "
            		+ "percentage value between [0 and 1[");
        }

        setGpu(Gpu.NULL);
        this.vgpuMigrationGpuOverhead = vgpuMigrationGpuOverhead;
    }
    
    @Override
    public double getVGpuMigrationGpuOverhead () {
    	return vgpuMigrationGpuOverhead;
    }

    @Override
	public Gpu getGpu () {
    	return gpu;
    }

    @Override
	public VGpuScheduler setGpu (Gpu gpu) {
    	if(isOtherGpuAssigned(requireNonNull(gpu))){
            throw new IllegalStateException("VgpuScheduler already has a Gpu assigned to it. "
            		+ "Each Gpu must have its own VgpuScheduler instance.");
        }

        this.gpu = gpu;
        return this;
    }
    
    private boolean isOtherGpuAssigned (final Gpu gpu) {
        return this.gpu != null && this.gpu != Gpu.NULL && gpu != this.gpu;
    }
    
    @Override
    public final boolean isSuitableForVGpu (final CustomVGpu vgpu) {
        return isSuitableForVGpu(vgpu, vgpu.getCurrentRequestedMips());
    }

    @Override
    public final boolean isSuitableForVGpu (final CustomVGpu vgpu, 
    		final MipsShare requestedMips) {
        if(requestedMips.isEmpty()){
            LOGGER.warn(
                "{}: {}: It was requested an empty list of COREs for {} in {}",
                gpu.getSimulation().clockStr(), getClass().getSimpleName(), vgpu, gpu);
            return false;
        }

        if (gpu.isFailed()){
            return false;
        }

        return isSuitableForVGpuInternal(vgpu, requestedMips);
    }

    protected abstract boolean isSuitableForVGpuInternal (
    		CustomVGpu vgpu, MipsShare requestedMips);

    @Override
    public final boolean allocateCoresForVGpu (final CustomVGpu vgpu) {
        return allocateCoresForVGpu (vgpu, new MipsShare(vgpu.getVGpuCore().getCapacity(), 
        		vgpu.getVGpuCore().getMips()));
    }

    @Override
    public final boolean allocateCoresForVGpu (final CustomVGpu vgpu, 
    		final MipsShare requestedMips) {
        if (!vgpu.isInMigration() && gpu.getVGpusMigratingOut().contains(vgpu)) {
            gpu.removeVGpuMigratingOut(vgpu);
        }

        ((CustomVGpuSimple)vgpu).setRequestedMips(new MipsShare(requestedMips));
        if(allocateCoresForVGpuInternal(vgpu, requestedMips)) {
            updateGpuCoresStatusToBusy(vgpu);
            return true;
        }
        return false;
    }
    
    protected abstract boolean allocateCoresForVGpuInternal (CustomVGpu vgpu, 
    		MipsShare mipsShareRequested);
    
    private void updateGpuCoresStatusToBusy (final CustomVGpu vgpu) {
        updateGpuCoresStatus (gpu.getFreeCoreList(), vgpu.getNumberOfCores(), Pe.Status.BUSY);
    }
    
    private void updateGpuCoresStatus (final List<Pe> coreList, 
    		final long vCoresNumber, final Pe.Status newStatus) {
        if(vCoresNumber <= 0) 
            return;

        final var selectedCoresList = coreList.stream().limit(vCoresNumber).collect(toList());
        ((GpuSimple)gpu).setCoreStatus(selectedCoresList, newStatus);
    }
    
    @Override
    public void deallocateCoresFromVGpu (final CustomVGpu vgpu) {
        deallocateCoresFromVGpu(vgpu, (int)vgpu.getNumberOfCores());
    }

    @Override
    public void deallocateCoresFromVGpu (final CustomVGpu vgpu, final int coresToRemove) {
        if(coresToRemove <= 0 || vgpu.getNumberOfCores() == 0){
            return;
        }

        final long removedCores = deallocateCoresFromVGpuInternal(vgpu, coresToRemove);
        updateGpuUsedCoresToFree(removedCores);
    }
    
    private void updateGpuUsedCoresToFree (final long removedCores) {
        updateGpuCoresStatus(gpu.getBusyCoreList(), removedCores, Pe.Status.FREE);
    }
    
    protected final long removeCoresFromVGpu (final CustomVGpu vgpu, final MipsShare mipsShare, 
    		final long coresToRemove) {
        return mipsShare.remove(Math.min(vgpu.getNumberOfCores(), coresToRemove));
    }

    protected abstract long deallocateCoresFromVGpuInternal(CustomVGpu vgpu, int pesToRemove);

    @Override
    public MipsShare getAllocatedMips (final CustomVGpu vgpu) {
        final MipsShare mipsShare = ((CustomVGpuSimple)vgpu).getAllocatedMips();

        return gpu.getVGpusMigratingOut().contains(vgpu) ? getMipsShareRequestedReduced(
        		vgpu, mipsShare) : mipsShare;
    }
    
    protected MipsShare getMipsShareRequestedReduced(final CustomVGpu vgpu, 
    		final MipsShare mipsShareRequested){
        final double peMips = getCoreCapacity();
        final long requestedPes = mipsShareRequested.pes();
        final double requestedMips = mipsShareRequested.mips();
        return new MipsShare(requestedPes, 
        		Math.min(requestedMips, peMips) * percentOfMipsToRequest(vgpu));
    }
    
    @Override
    public double getTotalAllocatedMipsForVGpu (final CustomVGpu vgpu) {
        return getAllocatedMips(vgpu).totalMips();
    }
    
    public long getCoreCapacity () {
        return getWorkingCoreList().isEmpty() ? 0 : getWorkingCoreList().get(0).getCapacity();
    }
    
    public final List<Pe> getWorkingCoreList () {
        return gpu.getWorkingCoreList();
    }
    
    @Override
    public MipsShare getRequestedMips (final CustomVGpu vgpu) {
        return ((CustomVGpuSimple)vgpu).getRequestedMips();
    }

    @Override
    public double getTotalAvailableMips () {
        final var vgpuStream = Stream.concat(gpu.getVGpuList().stream(),
        		gpu.getVGpusMigratingIn().stream());
        final double allocatedMips =
                vgpuStream
                    .map(vgpu -> (CustomVGpuSimple)vgpu)
                    .mapToDouble(this::actualVGpuTotalRequestedMips)
                    .sum();

        return gpu.getTotalMipsCapacity() - allocatedMips;
    }
    
    private double actualVGpuTotalRequestedMips (final CustomVGpuSimple vgpu) {
        final double totalVGpuRequestedMips = vgpu.getAllocatedMips().totalMips();

        return totalVGpuRequestedMips / percentOfMipsToRequest(vgpu);
    }
    
    protected double percentOfMipsToRequest (final CustomVGpu vgpu) {
        if (gpu.getVGpusMigratingIn().contains(vgpu)) {
            /* While the VM is migrating in,
            the destination host only increases CPU usage according
            to the CPU migration overhead.*/
             
            return vgpuMigrationGpuOverhead;
        }

        if (gpu.getVGpusMigratingOut().contains(vgpu)) {
            /* While the VM is migrating out, the host where it's migrating from
            experiences a performance degradation.
            Thus, the allocated MIPS for that VM is reduced according to the CPU migration
            overhead.*/
            return getMaxGpuUsagePercentDuringOutMigration();
        }

        //VM is not migrating, thus 100% of its requested MIPS will be requested to the Host.
        return 1;
    }
    
    @Override
    public double getMaxGpuUsagePercentDuringOutMigration() {
        return 1 - vgpuMigrationGpuOverhead;
    }

}
