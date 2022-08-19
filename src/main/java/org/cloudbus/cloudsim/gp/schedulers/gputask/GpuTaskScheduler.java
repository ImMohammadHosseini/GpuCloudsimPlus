package org.cloudbus.cloudsim.gp.schedulers.gputask;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface GpuTaskScheduler extends Serializable {
	Logger LOGGER = LoggerFactory.getLogger(GpuTaskScheduler.class.getSimpleName());
	
	GpuTaskScheduler NULL = new GpuTaskSchedulerNull();
	
	
}

