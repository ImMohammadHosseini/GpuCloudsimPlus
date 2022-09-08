package org.gpucloudsimplus.listeners;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.EventInfo;

public interface GpuTaskVGpuEventInfo extends GpuTaskEventInfo, VGpuEventInfo {
	
	static GpuTaskVGpuEventInfo of (final EventListener<? extends EventInfo> listener, 
			final GpuTask gpuTask, final VGpu vgpu) {
        return of(listener, gpuTask.getSimulation().clock(), gpuTask, vgpu);
    }

    static GpuTaskVGpuEventInfo of (final EventListener<? extends EventInfo> listener, 
    		final double time, final GpuTask gpuTask) {
        return of(listener, time, gpuTask, gpuTask.getVGpu());
    }
    
    static GpuTaskVGpuEventInfo of (final EventListener<? extends EventInfo> listener, 
    		final GpuTask gpuTask) {
        return of(listener, gpuTask, gpuTask.getVGpu());
    }

    static GpuTaskVGpuEventInfo of (final EventListener<? extends EventInfo> listener, 
    		final double time, final GpuTask gpuTask, final VGpu vgpu){
        return new GpuTaskVGpuEventInfo () {
        	@Override public GpuTask getGpuTask () { return gpuTask; }
            @Override public VGpu getVGpu () { return vgpu; }
            @Override public double getTime () { return time; }
            @Override public EventListener<? extends EventInfo> getListener () { return listener; }
        };
    }

}
