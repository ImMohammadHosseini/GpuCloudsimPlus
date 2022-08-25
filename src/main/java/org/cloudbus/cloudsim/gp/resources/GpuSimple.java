package org.cloudbus.cloudsim.gp.resources;

import java.util.*;
import static java.util.Objects.requireNonNull;

import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.util.BytesConversion;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;
import org.cloudbus.cloudsim.resources.ResourceManageableAbstract;
import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisioner;
import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisionerSimple;



public class GpuSimple implements Gpu {
	
	private static long defaultRamCapacity = (long) BytesConversion.gigaToMega(10);
    private static long defaultBwCapacity = 1000;
    
	private long id;
	private String type;
	private final Ram ram;
    private final Bandwidth bw;
	private List<Pe> gpuCoreList;
	private GpuResourceProvisioner gpuGddramProvisioner;
	private GpuResourceProvisioner gpuBwProvisioner;
	
    private final List<GpuStateHistoryEntry> stateHistory;

    private boolean activateOnVideocardStartup;
    private boolean failed;
    private boolean active;
    private boolean stateHistoryEnabled;
    private double startTime = -1;
    private double firstStartTime = -1;
    private double shutdownTime;
    private double totalUpTime;
    private double lastBusyTime;
    private double idleShutdownDeadline;
    
    private VGpuScheduler vgpuScheduler;
    private final List<CustomVGpu> vgpuList = new ArrayList<>();

    private final Set<CustomVGpu> vgpusMigratingIn;
    private final Set<CustomVGpu> vgpusMigratingOut;
    
    private Videocard videocard;

    private final Set<EventListener<HostUpdatesVmsProcessingEventInfo>> onUpdateProcessingListeners;
    private final List<EventListener<GpuEventInfo>> onStartupListeners;
    private final List<EventListener<GpuEventInfo>> onShutdownListeners;

    private List<ResourceManageable> resources;

    private List<GpuResourceProvisioner> provisioners;
    private final List<CustomVGpu> vgpuCreatedList;
    
    private int freeCoresNumber;
    private int busyCoresNumber;
    private int workingCoresNumber;
    private int failedCoresNumber;
    private boolean lazySuitabilityEvaluation;
	
    
    
    
	//need change the constructor and make it in 4 or 5 constructor
	public GpuSimple (long id, String type, final long ram, final long bw,
			final List<Pe> peList) {
		this.setId(id);
		this.setType(type);
		
		this.ram = new Ram(ram);
		this.bw = new Bandwidth(bw);
		
		//this.setGpuGddramProvisioner(new GpuResourceProvisionerSimple());
        this.setGpuBwProvisioner(new GpuResourceProvisionerSimple());
        this.setGpuCoreList(peList);
	}
	
	@Override 
	public final void setId (long id) {
		this.id = id;
	}
	@Override 
	public long getId () {
        return id;
    }
	
	public void setType (String type) {
		this.type = type;
	}
	
	public String getType () {
		return type;
	}
	
	public void setGpuCoreList (final List<Pe> coreList) {
		if(requireNonNull(coreList).isEmpty()){
            throw new IllegalArgumentException("The CORE list for a Gpu cannot be empty");
        }
		
		this.gpuCoreList = coreList;
		
		//need to be complete
	}

	@Override 
	public List<Pe> getGpuCoreList () {
		return gpuCoreList;
    }
	
	
	@Override 
	public Gpu setGpuGddramProvisioner (GpuResourceProvisioner gpuGddramProvisioner) {
		this.gpuGddramProvisioner = requireNonNull(gpuGddramProvisioner);
        this.gpuGddramProvisioner.setResources(ram, vgpu -> ((CustomVGpuSimple)vgpu).getGddram());
        return this;
	}
	
	@Override 
	public GpuResourceProvisioner getGpuGddramProvisioner () {
        return gpuGddramProvisioner;
    }
        
    @Override 
    public Gpu setGpuBwProvisioner (GpuResourceProvisioner gpuBwProvisioner) {
    	this.gpuBwProvisioner = requireNonNull(gpuBwProvisioner);
        this.gpuBwProvisioner.setResources(bw, vgpu -> ((CustomVGpuSimple)vgpu).getBw());
        //must add set resource in gpurespro //has to
    	return this;
    }
    
    @Override 
    public GpuResourceProvisioner getGpuBwProvisioner () {
        return gpuBwProvisioner;
    }
}