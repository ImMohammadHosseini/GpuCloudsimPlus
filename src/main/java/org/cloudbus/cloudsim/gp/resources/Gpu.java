
package org.cloudbus.cloudsim.gp.resources;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.gpucloudsimplus.listeners.GpuEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.core.ChangeableId;
import org.cloudbus.cloudsim.resources.Pe; 

import org.gpucloudsimplus.listeners.GpuUpdatesVgpusProcessingEventInfo;
import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisioner;
import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;
import org.cloudbus.cloudsim.gp.videocards.Videocard;

public interface Gpu extends ChangeableId {
	//, ResourceManageable
    Logger LOGGER = LoggerFactory.getLogger(Gpu.class.getSimpleName());
	
    double DEF_IDLE_SHUTDOWN_DEADLINE = -1;

	Gpu NULL = new GpuNull ();
    
	List<Pe> getGpuCoreList ();
	
	GpuResourceProvisioner getGpuGddramProvisioner ();
	
	Gpu setGpuGddramProvisioner (GpuResourceProvisioner gpuGddramProvisioner);
	
	GpuResourceProvisioner getGpuBwProvisioner ();
	
	Gpu setGpuBwProvisioner (GpuResourceProvisioner gpuBwProvisioner);
	
	Videocard getVideocard ();
	
    void setVideocard (Videocard videocard);

    boolean isSuitableForVgpu (CustomVGpu vgpu);

    GpuSuitability getSuitabilityFor (CustomVGpu vgpu);

    boolean isActive();
    
    boolean hasEverStarted ();
    
    Gpu setActive (boolean activate);

    <T extends CustomVGpu> Set<T> getVgpusMigratingIn ();

    boolean hasMigratingVgpus ();
    
    boolean addMigratingInVgpu (CustomVGpu vgpu);

    Set<CustomVGpu> getVgpusMigratingOut ();

    boolean addVgpuMigratingOut (CustomVGpu vgpu);

    boolean removeVgpuMigratingOut (CustomVGpu vgpu);

    void reallocateMigratingInVgpus ();

    //@Override
    double getTotalMipsCapacity ();

    double getTotalAvailableMips ();

    double getTotalAllocatedMips ();

    double getTotalAllocatedMipsForVgpu (CustomVGpu vgpu);

    void removeMigratingInVgpu (CustomVGpu vgpu);

    List<Pe> getWorkingCoreList ();

    List<Pe> getBusyCoreList ();

    List<Pe> getFreeCoreList ();

    int getFreeCoresNumber ();

    int getWorkingCoresNumber ();

    int getBusyCoresNumber ();

    double getBusyCoresPercent ();

    double getBusyCoresPercent (boolean hundredScale);

    int getFailedCoresNumber ();

    //long getAvailableStorage();

    <T extends CustomVGpu> List<T> getVgpuList ();

    <T extends CustomVGpu> List<T> getVgpuCreatedList ();

    VGpuScheduler getVgpuScheduler ();

    Gpu setVgpuScheduler (VGpuScheduler vgpuScheduler);

    double getFirstStartTime ();

    double getShutdownTime ();

    void setShutdownTime (double shutdownTime);

    //double getUpTime ();

    //double getUpTimeHours ();

    //double getTotalUpTimeHours ();

    double getIdleShutdownDeadline ();

    Gpu setIdleShutdownDeadline (double deadline);

    boolean isFailed ();

    boolean setFailed (boolean failed);

    double updateProcessing (double currentTime);

    GpuSuitability createVGpu (CustomVGpu vgpu);

    void destroyVGpu (CustomVGpu vgpu);

    GpuSuitability createTemporaryVGpu (CustomVGpu vgpu);

    void destroyTemporaryVm (CustomVGpu vgpu);

    void destroyAllVGpus ();

    Gpu addOnStartupListener(EventListener<GpuEventInfo> listener);

    boolean removeOnStartupListener (EventListener<GpuEventInfo> listener);

    Gpu addOnShutdownListener (EventListener<GpuEventInfo> listener);

    boolean removeOnShutdownListener (EventListener<GpuEventInfo> listener);

    Gpu addOnUpdateProcessingListener (EventListener<GpuUpdatesVgpusProcessingEventInfo> listener);

    boolean removeOnUpdateProcessingListener (EventListener<GpuUpdatesVgpusProcessingEventInfo> listener);

    //Gpu setSimulation(Simulation simulation);

    GpuResourceProvisioner getProvisioner (Class<? extends ResourceManageable> resourceClass);

    double getGpuCorePercentUtilization ();

    double getGpuCorePercentRequested ();

    //HostResourceStats getCpuUtilizationStats();

    void enableUtilizationStats ();

    double getGpuCoreMipsUtilization ();

    long getBwUtilization ();

    long getGddramUtilization ();

    //PowerModelHost getPowerModel ();

    //void setPowerModel (PowerModelHost powerModel);

    void enableStateHistory ();

    void disableStateHistory ();

    boolean isStateHistoryEnabled ();

    List<GpuStateHistoryEntry> getStateHistory ();

    List<CustomVGpu> getFinishedVGpus ();

    List<CustomVGpu> getMigratableVGpus ();

    boolean isLazySuitabilityEvaluation ();

    Gpu setLazySuitabilityEvaluation (boolean lazySuitabilityEvaluation);
}