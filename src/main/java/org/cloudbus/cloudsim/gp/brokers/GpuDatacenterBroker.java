package org.cloudbus.cloudsim.gp.brokers;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GpuDatacenterBroker extends DatacenterBroker {
	Logger LOGGER = LoggerFactory.getLogger(DatacenterBroker.class.getSimpleName());

    GpuDatacenterBroker NULL = new GpuDatacenterBrokerNull();
    
    double DEF_GPUVM_DESTRUCTION_DELAY = -1.0;

    double DEF_VGPU_DESTRUCTION_DELAY = -1.0;
    
    //boolean bindGpuTaskToVGpu (GpuTask gpuTask, VGpu vgpu);
    
    //DatacenterBroker requestIdleVGpuDestruction(VGpu vgpu);
    
}
