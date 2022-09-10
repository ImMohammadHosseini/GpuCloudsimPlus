package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface VGpuScheduler {
    Logger LOGGER = LoggerFactory.getLogger (VGpuScheduler.class.getSimpleName());

    double DEF_VGPU_MIGRATION_GPU_OVERHEAD = 0.1;

	VGpuScheduler NULL = new VGpuSchedulerNull ();
	
	boolean allocateCoresForVGpu (VGpu vgpu, MipsShare requestedMips);

    boolean allocateCoresForVGpu (VGpu vgpu);

    void deallocateCoresFromVGpu (VGpu vgpu);

    void deallocateCoresFromVGpu (VGpu vgpu, int coresToRemove);

    MipsShare getAllocatedMips (VGpu vgpu);

    double getTotalAvailableMips ();

	Gpu getGpu ();

	VGpuScheduler setGpu (Gpu gpu);

    MipsShare getRequestedMips (VGpu vgpu);

    boolean isSuitableForVGpu (VGpu vgpu);

    boolean isSuitableForVGpu (VGpu vgpu, MipsShare requestedMips);

    double getTotalAllocatedMipsForVGpu (VGpu vgpu);

    double getMaxGpuUsagePercentDuringOutMigration ();

	double getVGpuMigrationGpuOverhead ();

}
