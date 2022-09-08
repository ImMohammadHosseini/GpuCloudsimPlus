package org.gpucloudsimplus.listeners;

import org.cloudsimplus.listeners.EventListener;

import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

public interface VGpuVideocardEventInfo extends VGpuEventInfo, VideocardEventInfo {
    
    static VGpuVideocardEventInfo of (final EventListener<VGpuVideocardEventInfo> listener, 
    		final VGpu vgpu) {
        return of(listener, vgpu, vgpu.getGpu().getVideocard());
    }

    static VGpuVideocardEventInfo of(final EventListener<VGpuVideocardEventInfo> listener, 
    		final VGpu vgpu, final Videocard videocard) {
        final double time = vgpu.getSimulation().clock();
        return new VGpuVideocardEventInfo () {
            @Override public Videocard getVideocard () { return videocard; }
            @Override public VGpu getVGpu() { return vgpu; }
            @Override public double getTime() { return time; }
            @Override public EventListener<VGpuVideocardEventInfo> getListener() { return listener; }
        };
    }
}
