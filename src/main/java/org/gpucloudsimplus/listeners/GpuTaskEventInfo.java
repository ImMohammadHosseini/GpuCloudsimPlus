package org.gpucloudsimplus.listeners;

import org.cloudsimplus.listeners.EventInfo;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;

public interface GpuTaskEventInfo extends EventInfo {

    GpuTask getGpuTask();
}

