package org.cloudbus.cloudsim.gp.brokers;

import java.util.List;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GpuDatacenterBroker extends DatacenterBroker {
	Logger LOGGER = LoggerFactory.getLogger(DatacenterBroker.class.getSimpleName());

    GpuDatacenterBroker NULL = new GpuDatacenterBrokerNull();
    
    double DEF_VGPU_DESTRUCTION_DELAY = -1.0;
    
    boolean bindGpuTaskToVGpu (GpuTask gpuTask, CustomVGpu vgpu);
    
    DatacenterBroker requestIdleVGpuDestruction(CustomVGpu vgpu);
    
}
