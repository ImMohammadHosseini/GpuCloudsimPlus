package org.cloudbus.cloudsim.gp.hosts;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.util.BytesConversion;
import org.cloudbus.cloudsim.hosts.HostSuitability;
import org.cloudbus.cloudsim.resources.HarddriveStorage;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.videocards.VideocardSimple;
import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenterSimple;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicy;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicySimple;

import java.util.*;
import java.util.stream.Stream;
import java.util.function.Predicate;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

//  extends HostSimple 
public class GpuHostSimple extends HostSimple implements GpuHost {
	
	private static long defaultRamCapacity = (long) BytesConversion.gigaToMega(10);
    private static long defaultBwCapacity = 1000;
    private static long defaultStorageCapacity = (long) BytesConversion.gigaToMega(500);
    
	private Videocard videocard;
	
	
	public GpuHostSimple (final List<Pe> peList, final List<Gpu> gpuList) {
        this(peList, gpuList, true);
    }
	
    public GpuHostSimple (final List<Pe> peList, final List<Gpu> gpuList, final boolean activate) {
    	
        this(defaultRamCapacity, defaultBwCapacity, defaultStorageCapacity, peList, gpuList, activate,
        		new VGpuAllocationPolicySimple());
    }

    public GpuHostSimple (final ResourceProvisioner ramProvisioner, 
    		final ResourceProvisioner bwProvisioner, final long storage, final List<Pe> peList,
    		final List<Gpu> gpuList, final VGpuAllocationPolicy vgpuAllocationPolicyfinal) {
    	
        super(ramProvisioner.getCapacity(), bwProvisioner.getCapacity(), storage, peList);
        videocard = new VideocardSimple (gpuList, vgpuAllocationPolicyfinal);
    }

    public GpuHostSimple (final long ram, final long bw, final long storage, final List<Pe> peList,
    		final List<Gpu> gpuList) {
        this(ram, bw, new HarddriveStorage(storage), peList, gpuList);
    }

    public GpuHostSimple ( final long ram, final long bw, final HarddriveStorage storage, 
    		final List<Pe> peList, final List<Gpu> gpuList) {
        super(ram, bw, storage, peList);
        videocard = new VideocardSimple (gpuList);
    }

    public GpuHostSimple (final long ram, final long bw, final long storage,
    		final List<Pe> peList, final List<Gpu> gpuList, boolean activate, 
    		final VGpuAllocationPolicy vgpuAllocationPolicyfinal) {
        super(ram, bw, storage, peList, activate);
        videocard = new VideocardSimple (gpuList, vgpuAllocationPolicyfinal);     
    }


    /*@Override
    public GpuHost setActive (final boolean activate) {
        if(!activate) {
            activateOnGpuDatacenterStartup = false;
        }

        final double delay = activate ? powerModel.getStartupDelay() : powerModel.getShutDownDelay();
        if(this.active == activate || delay > 0 && activationChangeInProgress){
            return this;
        }

        if(isFailed() && activate){
            throw new IllegalStateException("The Host is failed and cannot be activated.");
        }

        if (delay == 0) {
           //If there is no delay, start up or shutdown the Host right away.
           processActivation(activate);
           return this;
        }

        //If the simulation is not running and there is a startup delay,
        // when the datacenter is started up, it will request such a Host activation. 
        if(!simulation.isRunning()){
            return this;
        }

        final CloudSimTag tag = activate ? CloudSimTag.HOST_POWER_ON : CloudSimTag.HOST_POWER_OFF;
        final String msg = (activate ? "on" : "off") + " (expected time: {} seconds).";
        LOGGER.info("{}: {} is being powered " + msg, getSimulation().clockStr(), this, delay);
        datacenter.schedule(delay, tag, this);
        activationChangeInProgress = true;

        return this;
    }*/
    
    @Override
    public void processActivation(final boolean activate) {
        super.processActivation(activate);
        videocard.gpusProcessActivation(activate);
        
    }

	@Override
	public Videocard getVideocard () {
		return videocard;
	}
	
}
