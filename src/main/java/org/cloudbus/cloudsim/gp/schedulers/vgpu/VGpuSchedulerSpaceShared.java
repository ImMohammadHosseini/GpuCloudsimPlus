package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.vgpu.VGpuSimple;
import org.cloudbus.cloudsim.gp.resources.GpuCore;
import org.cloudbus.cloudsim.schedulers.MipsShare;

import java.util.*;

public class VGpuSchedulerSpaceShared extends VGpuSchedulerAbstract {
	
	public VGpuSchedulerSpaceShared() {
        this(DEF_VGPU_MIGRATION_GPU_OVERHEAD);
    }

    public VGpuSchedulerSpaceShared(final double vgpuMigrationCpuOverhead){
        super(vgpuMigrationCpuOverhead);
    }

    @Override
    protected boolean isSuitableForVGpuInternal (final VGpu vgpu, final MipsShare requestedMips) {
        final List<GpuCore> selectedCores = getTotalCapacityToBeAllocatedToVGpu (requestedMips);
        return selectedCores.size() >= requestedMips.pes();
    }

    private List<GpuCore> getTotalCapacityToBeAllocatedToVGpu (final MipsShare requestedMips) {
        if (getGpu().getWorkingCoresNumber() < requestedMips.pes()) {
            return getGpu().getWorkingCoreList();
        }

        final List<GpuCore> freeCoreList = getGpu().getFreeCoreList();
        final List<GpuCore> selectedCores = new ArrayList<>();
        if(freeCoreList.isEmpty()){
            return selectedCores;
        }

        final Iterator<GpuCore> coreIterator = freeCoreList.iterator();
        GpuCore core = coreIterator.next();
        for (int i = 0; i < requestedMips.pes(); i++) {
            if (requestedMips.mips() <= core.getCapacity()) {
                selectedCores.add(core);
                if (!coreIterator.hasNext()) {
                    break;
                }
                core = coreIterator.next();
            }
        }

        return selectedCores;
    }

    @Override
    public boolean allocateCoresForVGpuInternal (final VGpu vgpu, final MipsShare requestedMips) {
        final List<GpuCore> selectedCores = getTotalCapacityToBeAllocatedToVGpu(requestedMips);
        if(selectedCores.size() < requestedMips.pes()){
            return false;
        }

        ((VGpuSimple)vgpu).setAllocatedMips(requestedMips);
        return true;
    }

    @Override
    protected long deallocateCoresFromVGpuInternal (final VGpu vgpu, final int coresToRemove) {
        return removeCoresFromVGpu(vgpu, ((VGpuSimple)vgpu).getAllocatedMips(), coresToRemove);
    }
}

