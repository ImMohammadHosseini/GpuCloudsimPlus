package org.cloudbus.cloudsim.gp.videocards;

import org.cloudbus.cloudsim.core.Simulation;
//import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicy;
import org.gpucloudsimplus.listeners.VideocardVGpuMigrationEventInfo;
import org.cloudbus.cloudsim.gp.provisioners.VideocardBwProvisioner;
import org.gpucloudsimplus.listeners.GpuEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;


public interface Videocard {
	
    Logger LOGGER = LoggerFactory.getLogger(Videocard.class.getSimpleName());

	Videocard NULL = new VideocardNull ();
	
	boolean processVGpuCreate (final VGpu vgpu);
	
	VGpuAllocationPolicy getVGpuAllocationPolicy ();
	
	String processVGpuDestroy (final VGpu vgpu);
	
	<T extends Gpu> List<T> getGpuList ();
	
	void updateGpusProcessing ();

	<T extends VGpu> List<T> getVGpuList ();
	
	void processGpuAdditionRequest ();
	
	void gpusProcessActivation (boolean activate);
	
	void gpuProcessActivation (Gpu gpu, boolean activate);
	//<T extends Gpu> void notifyOnGpuAvailableListeners (final T gpu);
	
	//double DEF_BW_PERCENT_FOR_MIGRATION = 0.5;
	
	/*long getId ();
	
	void setId (long id);
	
	String getType ();
	
	void setType (String type);*/
	
	//void videocardProcess ();
	
	boolean hasGpuHost ();
	
	Videocard setSimulation (Simulation simulation);
	
	
	Videocard setVGpuAllocationPolicy (VGpuAllocationPolicy vgpuAllocationPolicy);
	
	VideocardBwProvisioner getPcieBwProvisioner ();
	
	Videocard setPcieBwProvisioner (VideocardBwProvisioner pcieBwProvisioner);
	
	//void requestVGpuMigration (CustomVGpu sourceVGpu, Gpu targetGpu);

    //void requestVGpuMigration (CustomVGpu sourceVGpu);

    
    
    Stream<? extends Gpu> getActiveGpuStream ();

    Gpu getGpu (int index);

    long getActiveGpusNumber ();
    
    long size ();

    Gpu getGpuById (long id);

    <T extends Gpu> Videocard addGpuList (List<T> gpuList);

    <T extends Gpu> Videocard addGpu (T gpu);

    <T extends Gpu> Videocard removeGpu (T gpu);

    double getSchedulingInterval ();

    Videocard setSchedulingInterval (double schedulingInterval);

    double getBandwidthPercentForMigration ();

    void setBandwidthPercentForMigration (double bandwidthPercentForMigration);
    
    Videocard addOnGpuAvailableListener (EventListener<GpuEventInfo> listener);

    //Videocard addOnVGpuMigrationFinishListener (EventListener<VideocardVGpuMigrationEventInfo> listener);

    //Videocard enableMigrations ();

    //Videocard disableMigrations ();
        
    GpuHost getHost ();
    
    Videocard setHost (GpuHost host);

    Simulation getSimulation ();
    
    
    
    Videocard setGpuSearchRetryDelay (double delay);
    
    double getGpuSearchRetryDelay ();
    
    boolean isMigrationsEnabled ();


}
