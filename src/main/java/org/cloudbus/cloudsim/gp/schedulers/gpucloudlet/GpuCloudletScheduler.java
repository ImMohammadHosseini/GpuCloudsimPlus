package org.cloudbus.cloudsim.gp.schedulers.gpucloudlet;

import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;

import java.util.*;
import org.slf4j.Logger;
import java.io.Serializable;
import org.slf4j.LoggerFactory;


public interface GpuCloudletScheduler extends Serializable, CloudletScheduler {
	Logger LOGGER = LoggerFactory.getLogger(GpuCloudletScheduler.class.getSimpleName());

	GpuCloudletScheduler NULL = new GpuCloudletSchedulerNull();
	
	
}

