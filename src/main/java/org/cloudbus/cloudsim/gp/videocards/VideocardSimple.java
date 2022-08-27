package org.cloudbus.cloudsim.gp.videocards;

import org.cloudbus.cloudsim.gp.provisioners.VideocardBwProvisioner;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicy;
import org.gpucloudsimplus.listeners.VideocardVGpuMigrationEventInfo;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.gpucloudsimplus.listeners.GpuEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;
import org.cloudbus.cloudsim.core.Simulation;

import java.util.*;

public class VideocardSimple implements Videocard {
	
	//private long id;
	//private String type;
	private GpuHost host;
	//private Simulation simulation;
	
	private List<? extends Gpu> gpuList;
	
	private VGpuAllocationPolicy vgpuAllocationPolicy;
	private VideocardBwProvisioner pcieBwProvisioner;
	
	private double schedulingInterval;
	
	private final List<EventListener<GpuEventInfo>> onGpuAvailableListeners;
    private final List<EventListener<VideocardVGpuMigrationEventInfo>> onVGpuMigrationFinishListeners;

    private Map<CustomVGpu, Gpu> lastMigrationMap;

    private double gpuSearchRetryDelay;
    
    private long activeGpusNumber;
    
    private double bandwidthPercentForMigration;
    
	public VideocardSimple (final List<? extends Gpu> gpuList,
	        final VGpuAllocationPolicy vgpuAllocationPolicy) {
		
		setGpuList(gpuList);
        setLastProcessTime(0.0);
        setSchedulingInterval(0);
        //setPowerModel(new PowerModelDatacenterSimple(this));
        
        this.onGpuAvailableListeners = new ArrayList<>();
        this.onVGpuMigrationFinishListeners = new ArrayList<>();
        this.bandwidthPercentForMigration = DEF_BW_PERCENT_FOR_MIGRATION;
        //this.migrationsEnabled = true;
        this.gpuSearchRetryDelay = -1;

        this.lastMigrationMap = Collections.emptyMap();

        setVGpuAllocationPolicy(vgpuAllocationPolicy);
	}
	
	/*@Override 
	public long getId () {
		return id;
	}
	
	@Override 
	public void setId (long id) {
		this.id = id;
	}
	
	@Override 
	public String getType () {
		return type;
	}
	
	@Override 
	public void setType (String type) {
		this.type = type;
	}*/
	
	@Override 
	public VGpuAllocationPolicy getVGpuAllocationPolicy () {
		return vgpuAllocationPolicy;
	}
	
	@Override 
	public Videocard setVGpuAllocationPolicy (VGpuAllocationPolicy vgpuAllocationPolicy) {
		
	}
	
	@Override 
	public VideocardBwProvisioner getPcieBwProvisioner () {
		return pcieBwProvisioner;
	}
	
	@Override public Videocard setPcieBwProvisioner (VideocardBwProvisioner pcieBwProvisioner) {
		
	}
}

