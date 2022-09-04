package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.core.GResourceStats;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

import java.util.function.Function;

public class VGpuResourceStats extends GResourceStats<CustomVGpu> {
    public static final VGpuResourceStats NULL = new VGpuResourceStats 
    		(CustomVGpu.NULL, vgpu -> 0.0) { 
    	@Override public boolean add(double time) { return false; }};

    		
    public VGpuResourceStats(final CustomVGpu machine, 
    		final Function<CustomVGpu, Double> resourceUtilizationFunction) {
        super(machine, resourceUtilizationFunction);
    }
}
