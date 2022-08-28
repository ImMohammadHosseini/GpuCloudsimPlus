package org.cloudbus.cloudsim.gp.hosts;

import org.cloudbus.cloudsim.hosts.Host;

public interface GpuHost extends Host {
	
	Logger LOGGER = LoggerFactory.getLogger(GpuHost.class.getSimpleName());
	
	double DEF_IDLE_SHUTDOWN_DEADLINE = -1;

    GpuHost NULL = new GpuHostNull();
}


