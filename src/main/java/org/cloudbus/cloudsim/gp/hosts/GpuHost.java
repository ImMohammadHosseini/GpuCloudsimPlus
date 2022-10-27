package org.cloudbus.cloudsim.gp.hosts;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSuitability;
import org.cloudbus.cloudsim.gp.vms.GpuVm;

import org.cloudbus.cloudsim.gp.videocards.Videocard;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;

public interface GpuHost extends Host {
	
	//Logger LOGGER = LoggerFactory.getLogger(GpuHost.class.getSimpleName());
	
	double DEF_IDLE_SHUTDOWN_DEADLINE = -1;

    GpuHost NULL = new GpuHostNull();
    
    Videocard getVideocard ();
    
    boolean hasVideocard ();
    
    GpuHost setVideocard (Videocard videocard);
    
    @Override
    <T extends Vm> List<T> getVmList ();
}


