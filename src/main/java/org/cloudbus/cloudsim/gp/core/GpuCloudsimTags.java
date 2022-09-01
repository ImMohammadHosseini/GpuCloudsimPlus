package org.cloudbus.cloudsim.gp.core;


public enum GpuCloudsimTags implements Comparable<GpuCloudsimTags> {
	
	
	VGPU_DESTROY,
	
	VGPU_MIGRATE,
		
	VGPU_DESTROY_ACK,
	
	VGPU_CREATE_ACK,
	
	VGPU_UPDATE_GPUTASK_PROCESSING,
	
	GPU_REMOVE,
	
	GPU_ADD,
	
	GPU_POWER_ON,
	
	GPU_POWER_OFF,
	
	GPUTASK_RETURN,
		
	GPUTASK_CANCEL,
	
	GPUTASK_PAUSE,
	
	GPUTASK_RESUME,
	
	GPUTASK_PAUSE_ACK,
	
	GPUTASK_RESUME_ACK,
	
	GPUTASK_SUBMIT_ACK;
	
	
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

