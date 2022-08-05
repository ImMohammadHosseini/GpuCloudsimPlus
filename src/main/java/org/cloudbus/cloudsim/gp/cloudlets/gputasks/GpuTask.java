package org.cloudbus.cloudsim.gp.cloudlets.gputasks;

import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudletSimple;

public interface GpuTask {
	
	enum Status {
        INSTANTIATED,
        READY,
        QUEUED,
        FROZEN,
        INEXEC,
        SUCCESS,
        FAILED,
        CANCELED,
        PAUSED,
        RESUMED,
        FAILED_RESOURCE_UNAVAILABLE
    }
	
	GpuCloudletSimple getGpuCloudlet ();
	
	void setGpuCloudlet (GpuCloudletSimple GpuCloudlet);
}
