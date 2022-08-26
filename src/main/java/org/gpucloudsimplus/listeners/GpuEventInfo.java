package org.gpucloudsimplus.listeners;

import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.EventInfo;

public interface GpuEventInfo extends EventInfo {

    Gpu getGpu ();

    static GpuEventInfo of (final EventListener<? extends EventInfo> listener, 
    		final Gpu gpu, final double time) {
        return new GpuEventInfo() {
            @Override public Gpu getGpu () { return gpu; }
            @Override public double getTime () { return time; }
            @Override public EventListener<? extends EventInfo> getListener () { return listener; }
        };
    }
}
