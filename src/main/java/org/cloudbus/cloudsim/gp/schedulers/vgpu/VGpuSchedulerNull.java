package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

public class VGpuSchedulerNull implements VGpuScheduler {
	
	@Override public boolean allocateCoresForVGpu (VGpu vgpu, MipsShare requestedMips) {
        return false;
    }
    @Override public boolean allocateCoresForVGpu (VGpu vgpu) { return false; }
    @Override public MipsShare getAllocatedMips (VGpu vgpu) {
        return MipsShare.NULL;
    }
    @Override public double getTotalAvailableMips() {
        return 0.0;
    }
    @Override public MipsShare getRequestedMips (VGpu vgpu) { return MipsShare.NULL; }
    @Override public double getTotalAllocatedMipsForVGpu (VGpu vgpu) {
        return 0.0;
    }
    @Override public double getMaxGpuUsagePercentDuringOutMigration() { return 0; }
    @Override public boolean isSuitableForVGpu (VGpu vgpu) {
        return false;
    }
    @Override public boolean isSuitableForVGpu (VGpu vgpu, MipsShare requestedMips) { 
    	return false; 
    }
    @Override public double getVGpuMigrationGpuOverhead() { return 0.0; }
    @Override public Gpu getGpu () {
        return Gpu.NULL;
    }
    @Override public VGpuScheduler setGpu (Gpu gpu) {
        return this;
    }
    @Override public void deallocateCoresFromVGpu (VGpu vgpu) {/**/}
    @Override public void deallocateCoresFromVGpu (VGpu vgpu, int coresToRemove) {/**/}
}

