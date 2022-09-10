package org.cloudbus.cloudsim.gp.brokers;

import java.util.List;
import java.util.function.Function;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GpuDatacenterBroker extends DatacenterBroker {
	Logger LOGGER = LoggerFactory.getLogger(DatacenterBroker.class.getSimpleName());

    GpuDatacenterBroker NULL = new GpuDatacenterBrokerNull();
    
    double DEF_VGPU_DESTRUCTION_DELAY = -1.0;
    
    //boolean bindGpuTaskToVGpu (GpuTask gpuTask, VGpu vgpu);
    
    //DatacenterBroker requestIdleVGpuDestruction(VGpu vgpu);
    
}
