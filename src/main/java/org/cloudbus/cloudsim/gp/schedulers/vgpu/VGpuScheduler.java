package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.gp.resources.Gpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface VGpuScheduler {
    Logger LOGGER = LoggerFactory.getLogger (VGpuScheduler.class.getSimpleName());

	VGpuScheduler NULL = new VGpuSchedulerNull ();
	
	boolean allocateCoresForVGpu (CustomVGpu vgpu, MipsShare requestedMips);

    boolean allocateCoresForVGpu (CustomVGpu vgpu);

    void deallocateCoresFromVGpu (CustomVGpu vgpu);

    void deallocateCoresFromVGpu (CustomVGpu vgpu, int coresToRemove);

    MipsShare getAllocatedMips (CustomVGpu vgpu);

    double getTotalAvailableMips ();

	Gpu getGpu ();

	VGpuScheduler setGpu (Gpu gpu);

    MipsShare getRequestedMips (CustomVGpu vgpu);

    boolean isSuitableForVGpu (CustomVGpu vgpu);

    boolean isSuitableForVGpu (CustomVGpu vgpu, MipsShare requestedMips);

    double getTotalAllocatedMipsForVGpu (CustomVGpu vgpu);

    double getMaxGpuUsagePercentDuringOutMigration ();

	double getVGpuMigrationGpuOverhead ();

}
