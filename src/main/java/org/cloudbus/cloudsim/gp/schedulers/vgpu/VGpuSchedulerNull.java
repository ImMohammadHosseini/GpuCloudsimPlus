package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.gp.resources.Gpu;

public class VGpuSchedulerNull implements VGpuScheduler {
	
	@Override public boolean allocateCoresForVGpu (CustomVGpu vgpu, MipsShare requestedMips) {
        return false;
    }
    @Override public boolean allocateCoresForVGpu (CustomVGpu vgpu) { return false; }
    @Override public MipsShare getAllocatedMips (CustomVGpu vgpu) {
        return MipsShare.NULL;
    }
    @Override public double getTotalAvailableMips() {
        return 0.0;
    }
    @Override public MipsShare getRequestedMips (CustomVGpu vgpu) { return MipsShare.NULL; }
    @Override public double getTotalAllocatedMipsForVGpu (CustomVGpu vgpu) {
        return 0.0;
    }
    @Override public double getMaxGpuUsagePercentDuringOutMigration() { return 0; }
    @Override public boolean isSuitableForVGpu (CustomVGpu vgpu) {
        return false;
    }
    @Override public boolean isSuitableForVGpu (CustomVGpu vgpu, MipsShare requestedMips) { 
    	return false; 
    }
    @Override public double getVGpuMigrationGpuOverhead() { return 0.0; }
    @Override public Gpu getGpu () {
        return Gpu.NULL;
    }
    @Override public VGpuScheduler setGpu (Gpu gpu) {
        return this;
    }
    @Override public void deallocateCoresFromVGpu (CustomVGpu vgpu) {/**/}
    @Override public void deallocateCoresFromVGpu (CustomVGpu vgpu, int coresToRemove) {/**/}
}

