package org.gpucloudsimplus.listeners;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudsimplus.listeners.EventListener;


public interface GpuUpdatesVgpusProcessingEventInfo extends GpuEventInfo  {
    
    double getNextGpuTaskCompletionTime ();

    static GpuUpdatesVgpusProcessingEventInfo of (
    		final EventListener<GpuUpdatesVgpusProcessingEventInfo> listener, 
    		final Gpu gpu, 
    		final double nextCloudletCompletionTime) {
        final double time = gpu.getSimulation().clock();
        return new GpuUpdatesVgpusProcessingEventInfo () {
            @Override public double getNextGpuTaskCompletionTime () { return nextCloudletCompletionTime; }
            @Override public Gpu getGpu () { return gpu; }
            @Override public double getTime() { return time; }
            @Override public EventListener<GpuUpdatesVgpusProcessingEventInfo> getListener () { 
            	return listener; }
        };
    }
}
