package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.vgpu.VGpuSimple;
import org.cloudbus.cloudsim.gp.resources.GpuCore;
import org.cloudbus.cloudsim.schedulers.MipsShare;

import java.util.*;


public class VGpuSchedulerTimeShared extends VGpuSchedulerAbstract {
	
	public VGpuSchedulerTimeShared () {
        this(DEF_VGPU_MIGRATION_GPU_OVERHEAD);
    }

    public VGpuSchedulerTimeShared (final double vgpuMigrationCpuOverhead){
        super(vgpuMigrationCpuOverhead);
    }

    @Override
    public boolean allocateCoresForVGpuInternal (final VGpu vgpu, final MipsShare requestedMips) {
        return allocateMipsShareForVGpuInternal (vgpu, requestedMips);
    }

    private boolean allocateMipsShareForVGpuInternal (final VGpu vgpu, final MipsShare requestedMips) {
        if (!isSuitableForVGpu(vgpu, requestedMips)) {
            return false;
        }

        allocateMipsShareForVGpu(vgpu, requestedMips);
        return true;
    }

    protected void allocateMipsShareForVGpu (final VGpu vgpu, final MipsShare requestedMipsReduced) {
        final var mipsShare = getMipsShareToAllocate(vgpu, requestedMipsReduced);
        ((VGpuSimple)vgpu).setAllocatedMips(mipsShare);
    }

    private void allocateCoresListForVGpu (final VGpu vgpu, final MipsShare mipsShare) {
        final Iterator<GpuCore> gpuCoresIterator = getWorkingCoreList().iterator();
        for (int i = 0; i < mipsShare.pes(); i++) {
            final double allocatedCoreMips = allocateMipsFromGpuCoresToGivenVirtualCore (vgpu, 
            		mipsShare.mips(), gpuCoresIterator);
            if(mipsShare.mips() > 0.1 && allocatedCoreMips <= 0.1){
                logMipsUnavailable(vgpu, mipsShare.mips(), allocatedCoreMips);
            }
        }
    }

    private void logMipsUnavailable (final VGpu vgpu, final double requestedMipsForVGpuCore, 
    		final double allocatedMipsForVGpuCore) {
        final String msg = allocatedMipsForVGpuCore > 0 ?
                String.format("Only %.0f MIPS were allocated.", allocatedMipsForVGpuCore)
                : "No MIPS were allocated.";
        LOGGER.warn(
                "{}: {}: {} is requiring a total of {} MIPS but the PEs of {} currently don't have such an available MIPS amount. {}",
                getGpu().getSimulation().clockStr(),
                getClass().getSimpleName(), vgpu,
                (long)requestedMipsForVGpuCore, getGpu(), msg);
    }

    private double allocateMipsFromGpuCoresToGivenVirtualCore (final VGpu vgpu, 
    		final double requestedMipsForVGpuCore, final Iterator<GpuCore> gpuCoresIterator)
    {
        if (requestedMipsForVGpuCore <= 0){
            return 0;
        }

        double allocatedMipsForVGpuCore = 0;
        
        while (allocatedMipsForVGpuCore <= 0 && gpuCoresIterator.hasNext()) {
             final GpuCore selectedGpuCore = gpuCoresIterator.next();
             if(allocateAllVGpuCoreRequestedMipsFromGpuCore(vgpu, selectedGpuCore, 
            		 requestedMipsForVGpuCore)){
                allocatedMipsForVGpuCore = requestedMipsForVGpuCore;
             } else {
                allocatedMipsForVGpuCore += allocatedAvailableMipsFromGpuCoreToVirtualCore (vgpu, 
                		selectedGpuCore);
             }
        }

        return allocatedMipsForVGpuCore;
    }

    private double allocatedAvailableMipsFromGpuCoreToVirtualCore (final VGpu vgpu, 
    		final GpuCore gpuCore) {
        final double availableMips = getAvailableMipsFromGpuCore (gpuCore);
        if (availableMips <= 0){
           return 0;
        }

        allocateMipsFromGpuCoreForVGpu (vgpu, gpuCore, availableMips);
        return availableMips;
    }

    private boolean allocateAllVGpuCoreRequestedMipsFromGpuCore (final VGpu vgpu, 
    		final GpuCore gpuCore, final double requestedMipsForVGpuCore) {
        if (getAvailableMipsFromGpuCore (gpuCore) >= requestedMipsForVGpuCore) {
            allocateMipsFromGpuCoreForVGpu (vgpu, gpuCore, requestedMipsForVGpuCore);
            return true;
        }

        return false;
    }

    private long getAvailableMipsFromGpuCore (final GpuCore gpuCore) {
        return gpuCore.getCoreProvisioner().getAvailableResource();
    }

    @Override
    protected boolean isSuitableForVGpuInternal (final VGpu vgpu, final MipsShare requestedMips) {
        final double totalRequestedMips = requestedMips.totalMips();

        return getGpu().getWorkingCoresNumber() >= requestedMips.pes() && 
        		getTotalAvailableMips() >= totalRequestedMips;
    }

    private void allocateMipsFromGpuCoreForVGpu (final VGpu vgpu, final GpuCore gpuCore, 
    		final double mipsToAllocate) {
        gpuCore.getCoreProvisioner().allocateResourceForVGpu(vgpu, (long)mipsToAllocate);
    }

    protected MipsShare getMipsShareToAllocate (final VGpu vgpu, final MipsShare requestedMips) {
        return getMipsShareToAllocate (requestedMips, percentOfMipsToRequest(vgpu));
    }

    protected MipsShare getMipsShareToAllocate (final MipsShare requestedMips, 
    		final double scalingFactor) {
        if(scalingFactor == 1){
            return requestedMips;
        }

        return new MipsShare(requestedMips.pes(), requestedMips.mips()*scalingFactor);
    }

    @Override
    protected long deallocateCoresFromVGpuInternal(final VGpu vgpu, final int coresToRemove) {
        return Math.max(
            removeCoresFromVGpu(vgpu, ((VGpuSimple)vgpu).getRequestedMips(), coresToRemove),
            removeCoresFromVGpu(vgpu, ((VGpuSimple)vgpu).getAllocatedMips(), coresToRemove));
    }
}

