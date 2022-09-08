package org.gpucloudsimplus.listeners;

import org.cloudsimplus.listeners.EventInfo;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

public interface VGpuEventInfo extends EventInfo {

    VGpu getVGpu();
}
