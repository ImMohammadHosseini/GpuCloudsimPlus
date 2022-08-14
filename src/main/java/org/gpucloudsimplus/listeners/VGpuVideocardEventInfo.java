package org.gpucloudsimplus.listeners;

import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.cloudbus.cloudsim.gp.videocards.Videocard;

public interface VGpuVideocardEventInfo extends VGpuEventInfo, VideocardEventInfo {
	
	static VGpuVideocardEventInfo of(final EventListener<VGpuVideocardEventInfo> listener,
			final CustomVGpu vgpu, final Videocard videocard) {
		
		final double time = vgpu.getSimulation().clock();
		return new VGpuVideocardEventInfo() {
			@Override public Videocard getVideocard() { return videocard; }
			@Override public CustomVGpu getVGpu() { return vgpu; }
			@Override public double getTime() { return time; }
			@Override public EventListener<VGpuVideocardEventInfo> getListener() { return listener; }
	        };
	    }
}