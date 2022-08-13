package org.gpucloudsimplus.listeners;

import org.cloudsimplus.listeners.EventInfo;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

public interface VGpuEventInfo extends EventInfo {

    CustomVGpu getVm();
}
