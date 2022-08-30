package org.cloudbus.cloudsim.gp.brokers;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GpuDatacenterBroker extends DatacenterBroker {
	Logger LOGGER = LoggerFactory.getLogger(DatacenterBroker.class.getSimpleName());

    GpuDatacenterBroker NULL = new GpuDatacenterBrokerNull();
    
    boolean bindGpuTaskToVGpu (GpuTask gpuTask, CustomVGpu vgpu);
    
    
}
