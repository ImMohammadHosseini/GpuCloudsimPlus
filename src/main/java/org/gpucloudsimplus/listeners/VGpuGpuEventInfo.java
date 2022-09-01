package org.gpucloudsimplus.listeners;

import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.gp.resources.Gpu;

public interface VGpuGpuEventInfo extends VGpuEventInfo, GpuEventInfo {
	
	static VGpuGpuEventInfo of(final EventListener<VGpuGpuEventInfo> listener, 
			final CustomVGpu vgpu) {
        return of(listener, vgpu, vgpu.getGpu());
    }
	
	
	static VGpuGpuEventInfo of(final EventListener<VGpuGpuEventInfo> listener,
			final CustomVGpu vgpu, final Gpu gpu) {
		
		final double time = vgpu.getSimulation().clock();
		return new VGpuGpuEventInfo() {
			@Override public Gpu getGpu() { return gpu; }
			@Override public CustomVGpu getVGpu() { return vgpu; }
			@Override public double getTime() { return time; }
			@Override public EventListener<VGpuGpuEventInfo> getListener() { 
				return listener; }
		};
	}
}
