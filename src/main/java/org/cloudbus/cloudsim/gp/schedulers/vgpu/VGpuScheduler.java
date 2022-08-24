package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.schedulers.MipsShare;

import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.Bandwidth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface VGpuScheduler {
    Logger LOGGER = LoggerFactory.getLogger (VGpuScheduler.class.getSimpleName());

	VGpuScheduler NULL = new VGpuSchedulerNull ();
	
	double getVGpuMigrationGpuOverhead ();

	Videocard getVideocard ();

	VGpuScheduler setVideocard (Videocard videocard);
	
	boolean allocateGpuForVgpu (CustomVGpu vgpu, MipsShare requestedMips, 
			Ram gddramShare, Bandwidth bwShar);

    boolean allocateGpuForVgpu (CustomVGpu vgpu);

    void deallocateGpuFromVgpu (CustomVGpu vgpu);

    void deallocateGpuFromVgpu (CustomVGpu vgpu, int coresToRemove);

    MipsShare getAllocatedMips (CustomVGpu vgpu);
    
    double getTotalAvailableMips ();

    MipsShare getRequestedMips (CustomVGpu vgpu);

    boolean isSuitableForVgpu (CustomVGpu vgpu);

    boolean isSuitableForVgpu (CustomVGpu vgpu, MipsShare requestedMips);

    double getTotalAllocatedMipsForVgpu (CustomVGpu vgpu);

    double getMaxGpuUsagePercentDuringOutMigration ();

}
