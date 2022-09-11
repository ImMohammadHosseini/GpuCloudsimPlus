package org.cloudbus.cloudsim.gp.hosts;

import java.util.Objects;
import org.cloudbus.cloudsim.gp.resources.GpuSuitability;
import org.cloudbus.cloudsim.hosts.HostSuitability;

public final class GpuHostSuitability {
    public static final GpuHostSuitability NULL = new GpuHostSuitability();

    private boolean forStorage;
    private boolean forRam;
    private boolean forBw;
    private boolean forPes;
    private GpuSuitability forGpus;

    private String reason;

    public GpuHostSuitability () { /**/ }

    public GpuHostSuitability (final String reason) {
        this.reason = Objects.requireNonNull(reason);
    }

    public void setSuitability (final GpuHostSuitability other) {
        forPes = forPes && other.forPes;
        forRam = forRam && other.forRam;
        forBw = forBw && other.forBw;
        forStorage = forStorage && other.forStorage;
        forGpus.setSuitability(other.getForGpus());
    }

    public void setSuitability (final HostSuitability otherHost, final GpuSuitability otherGpu) {
        forPes = forPes && otherHost.forPes();
        forRam = forRam && otherHost.forRam();
        forBw = forBw && otherHost.forBw();
        forStorage = forStorage && otherHost.forStorage();
        forGpus.setSuitability(otherGpu);
    }
    
    public boolean forStorage () {
        return forStorage;
    }

    GpuHostSuitability setForStorage (final boolean suitable) {
        this.forStorage = suitable;
        return this;
    }

    public boolean forRam () {
        return forRam;
    }

    GpuHostSuitability setForRam (final boolean suitable) {
        this.forRam = suitable;
        return this;
    }

    public boolean forBw () {
        return forBw;
    }

    GpuHostSuitability setForBw(final boolean suitable) {
        this.forBw = suitable;
        return this;
    }

    public boolean forPes () {
        return forPes;
    }

    GpuHostSuitability setForPes (final boolean forPes) {
        this.forPes = forPes;
        return this;
    }

    public boolean forGpus () {
        return forGpus.fully();
    }
    
    public GpuSuitability getForGpus () {
    	return forGpus;
    }

    GpuHostSuitability setForGpus (final GpuSuitability forGpus) {
        this.forGpus = forGpus;
        return this;
    }
    
    public boolean fully () {
        return forStorage && forRam && forBw && forPes && forGpus.fully();
    }

    @Override
    public String toString(){
        if(fully())
            return "GpuHost is fully suitable for the last requested GpuVM";

        if(reason != null)
            return reason;

        final var builder = new StringBuilder("lack of");
        if(!forPes)
            builder.append(" PEs,");
        if(!forRam)
            builder.append(" RAM,");
        if(!forStorage)
            builder.append(" Storage,");
        if(!forBw)
            builder.append(" BW,");
        if(!forGpus.fully())
            builder.append(" Gpus,");

        return builder.substring(0, builder.length()-1);
    }
}
