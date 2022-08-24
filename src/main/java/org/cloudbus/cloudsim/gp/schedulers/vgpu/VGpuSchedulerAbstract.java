package org.cloudbus.cloudsim.gp.schedulers.vgpu;

import org.cloudbus.cloudsim.gp.resources.CustomVGpuSimple;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Ram;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public abstract class VGpuSchedulerAbstract implements VGpuScheduler {
	
	private Videocard videocard;
    private final double vgpuMigrationGpuOverhead;//vgpuMigrationCpuOverhead
    
    public VGpuSchedulerAbstract (final double vgpuMigrationGpuOverhead) {
        if(vgpuMigrationGpuOverhead < 0 || vgpuMigrationGpuOverhead >= 1){
            throw new IllegalArgumentException("vgpuMigrationGpuOverhead must be a percentage value between [0 and 1[");
        }

        setVideocard(Videocard.NULL);
        this.vgpuMigrationGpuOverhead = vgpuMigrationGpuOverhead;
    }
    
    @Override
    public double getVGpuMigrationGpuOverhead () {
    	return vgpuMigrationGpuOverhead;
    }

    @Override
	public Videocard getVideocard () {
    	return videocard;
    }

    @Override
	public VGpuScheduler setVideocard (Videocard videocard) {
    	if(isOtherVideocardAssigned(requireNonNull(videocard))){
            throw new IllegalStateException("VgpuScheduler already has a Videocard assigned to it. Each Videocard must have its own VgpuScheduler instance.");
        }

        this.videocard = videocard;
        return this;
    }
    
    private boolean isOtherVideocardAssigned (final Videocard videocard) {
        return this.videocard != null && this.videocard != Videocard.NULL && 
        		videocard != this.videocard;
    }
    
    @Override
    public boolean allocateGpuForVgpu (CustomVGpu vgpu, MipsShare requestedMips, 
			Ram gddramShare, Bandwidth bwShar) {
    	
    	if (!vgpu.isInMigration() && videocard.getVgpusMigratingOut().contains(vgpu)) {
    		videocard.removeVgpuMigratingOut(vgpu);
        }
    	/***/
    	((CustomVGpuSimple)vgpu).setRequestedMips(new MipsShare(requestedMips));
    	
    	return false;
    }
    
    @Override
    public boolean allocateGpuForVgpu (CustomVGpu vgpu) {
    	return allocateGpuForVgpu (vgpu, new MipsShare(vgpu.getVGpuCore().getCapacity(), 
    			vgpu.getVGpuCore().getMips()), ((CustomVGpuSimple) vgpu).getGddram(), 
    			((CustomVGpuSimple) vgpu).getBw());
    }
    
    
}
