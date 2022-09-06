package org.cloudbus.cloudsim.gp.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.gpucloudsimplus.listeners.GpuEventInfo;
import org.cloudbus.cloudsim.gp.core.AbstractGpu;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.core.ChangeableId;

import org.gpucloudsimplus.listeners.GpuUpdatesVgpusProcessingEventInfo;
import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisioner;
import org.cloudbus.cloudsim.gp.core.GpuResourceStatsComputer;
import org.cloudbus.cloudsim.gp.schedulers.vgpu.VGpuScheduler;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.resources.GpuCore;

public interface Gpu extends ChangeableId, Comparable<Gpu>, AbstractGpu, 
GpuResourceStatsComputer<GpuResourceStats> {
	//, ResourceManageable
    Logger LOGGER = LoggerFactory.getLogger(Gpu.class.getSimpleName());
	
    double DEF_IDLE_SHUTDOWN_DEADLINE = -1;

	Gpu NULL = new GpuNull ();
    
	List<GpuCore> getGpuCoreList ();
	
	GpuResourceProvisioner getGpuGddramProvisioner ();
	
	Gpu setGpuGddramProvisioner (GpuResourceProvisioner gpuGddramProvisioner);
	
	GpuResourceProvisioner getGpuBwProvisioner ();
	
	Gpu setGpuBwProvisioner (GpuResourceProvisioner gpuBwProvisioner);
	
	Videocard getVideocard ();
	
    void setVideocard (Videocard videocard);

    boolean isSuitableForVGpu (CustomVGpu vgpu);

    GpuSuitability getSuitabilityFor (CustomVGpu vgpu);

    boolean isActive();
    
    boolean hasEverStarted ();
    
    Gpu setActive (boolean activate);

    <T extends CustomVGpu> Set<T> getVGpusMigratingIn ();

    boolean hasMigratingVGpus ();
    
    boolean addMigratingInVGpu (CustomVGpu vgpu);

    Set<CustomVGpu> getVGpusMigratingOut ();

    boolean addVGpuMigratingOut (CustomVGpu vgpu);

    boolean removeVGpuMigratingOut (CustomVGpu vgpu);

    void reallocateMigratingInVGpus ();

    @Override
    double getTotalMipsCapacity ();

    double getTotalAvailableMips ();

    double getTotalAllocatedMips ();

    double getTotalAllocatedMipsForVGpu (CustomVGpu vgpu);

    void removeMigratingInVGpu (CustomVGpu vgpu);

    List<GpuCore> getWorkingCoreList ();

    List<GpuCore> getBusyCoreList ();

    List<GpuCore> getFreeCoreList ();

    int getFreeCoresNumber ();

    int getWorkingCoresNumber ();

    int getBusyCoresNumber ();

    double getBusyCoresPercent ();

    double getBusyCoresPercent (boolean hundredScale);

    int getFailedCoresNumber ();

    //long getAvailableStorage();

    <T extends CustomVGpu> List<T> getVGpuList ();

    <T extends CustomVGpu> List<T> getVGpuCreatedList ();

    VGpuScheduler getVGpuScheduler ();

    Gpu setVGpuScheduler (VGpuScheduler vgpuScheduler);

    double getFirstStartTime ();

    double getShutdownTime ();

    void setShutdownTime (double shutdownTime);

    double getUpTime ();

    double getUpTimeHours ();

    double getTotalUpTime();
    
    double getTotalUpTimeHours ();

    double getIdleShutdownDeadline ();

    Gpu setIdleShutdownDeadline (double deadline);

    boolean isFailed ();

    boolean setFailed (boolean failed);

    double updateProcessing (double currentTime);

    GpuSuitability createVGpu (CustomVGpu vgpu);

    void destroyVGpu (CustomVGpu vgpu);

    GpuSuitability createTemporaryVGpu (CustomVGpu vgpu);

    void destroyTemporaryVGpu (CustomVGpu vgpu);

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

    //void enableUtilizationStats ();

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

    void processActivation (boolean activate);
    
    Gpu setLazySuitabilityEvaluation (boolean lazySuitabilityEvaluation);
    
    //Simulation getSimulation ();
    
    //double getStartTime ();
    
    //Gpu setStartTime (final double startTime);
    
    //double getLastBusyTime ();
    
    //Resource getBw ();
    
    //Resource getGddram ();
    
    double getGpuPercentUtilization ();
    
    double getGpuPercentRequested ();
    
    //GpuResourceStats getGpuUtilizationStats ();
    
    double getGpuMipsUtilization ();
    
    default double getRelativeGpuUtilization (final CustomVGpu vgpu) {
        return getExpectedRelativeGpuUtilization(vgpu, vgpu.getGpuPercentUtilization());
    }

    default double getExpectedRelativeGpuUtilization (final CustomVGpu vgpu, 
    		final double vgpuGpuUtilizationPercent) {
        return vgpuGpuUtilizationPercent * getRelativeMipsCapacityPercent(vgpu);
    }
    
    default double getRelativeMipsCapacityPercent (final CustomVGpu vgpu) {
        return vgpu.getTotalMipsCapacity() / getTotalMipsCapacity();
    }

    default double getRelativeGddramUtilization (final CustomVGpu vgpu) {
        return vgpu.getGddram().getAllocatedResource() / (double)getGddram().getCapacity();
    }

    default double getRelativeBwUtilization(final CustomVGpu vgpu){
        return vgpu.getBw().getAllocatedResource() / (double)getBw().getCapacity();
    }
    
}