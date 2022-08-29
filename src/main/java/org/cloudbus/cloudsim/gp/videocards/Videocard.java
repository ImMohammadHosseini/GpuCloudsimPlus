package org.cloudbus.cloudsim.gp.videocards;

//import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;
import org.cloudbus.cloudsim.gp.allocationpolicies.VGpuAllocationPolicy;
import org.gpucloudsimplus.listeners.VideocardVGpuMigrationEventInfo;
import org.cloudbus.cloudsim.gp.provisioners.VideocardBwProvisioner;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;
import org.gpucloudsimplus.listeners.GpuEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.gp.resources.Gpu;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;


public interface Videocard {
    Logger LOGGER = LoggerFactory.getLogger(Videocard.class.getSimpleName());

	Videocard NULL = new VideocardNull ();
	
	double DEF_BW_PERCENT_FOR_MIGRATION = 0.5;
	
	/*long getId ();
	
	void setId (long id);
	
	String getType ();
	
	void setType (String type);*/
	
	VGpuAllocationPolicy getVGpuAllocationPolicy ();
	
	Videocard setVGpuAllocationPolicy (VGpuAllocationPolicy vgpuAllocationPolicy);
	
	VideocardBwProvisioner getPcieBwProvisioner ();
	
	Videocard setPcieBwProvisioner (VideocardBwProvisioner pcieBwProvisioner);
	
	void requestVGpuMigration (CustomVGpu sourceVGpu, Gpu targetGpu);

    void requestVGpuMigration (CustomVGpu sourceVGpu);

    <T extends Gpu> List<T> getGpuList ();
    
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

    Videocard addOnVGpuMigrationFinishListener (EventListener<VideocardVGpuMigrationEventInfo> listener);

    boolean isMigrationsEnabled ();

    Videocard enableMigrations ();

    Videocard disableMigrations ();
    
    double getGpuSearchRetryDelay ();

    Videocard setGpuSearchRetryDelay (double delay);
    
    GpuHost getHost ();

}
