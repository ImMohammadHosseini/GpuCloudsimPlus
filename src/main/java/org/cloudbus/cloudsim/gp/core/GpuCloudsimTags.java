package org.cloudbus.cloudsim.gp.core;


public enum GpuCloudsimTags implements Comparable<GpuCloudsimTags> {
	
	GPUTASK_SUBMIT_ACK,
	
	VGPU_MIGRATE,
	
	GPU_POWER_ON,
	
	GPU_POWER_OFF,
	
	GpuTask_RETURN,
	
	VGPU_UPDATE_GPUTASK_PROCESSING;
	
	
	private final int priority;

	GpuCloudsimTags () {
        this.priority = 0;
    }

    GpuCloudsimTags (final int priority) {
        this.priority = priority;
    }

    public int priority(){
        return priority;
    }

    public boolean between (final GpuCloudsimTags startInclusive, 
    		final GpuCloudsimTags endInclusive) {
        return this.ordinal() >= startInclusive.ordinal() && 
        		this.ordinal() <= endInclusive.ordinal();
    }
}

