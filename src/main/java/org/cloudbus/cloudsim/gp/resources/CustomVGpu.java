package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.vms.GpuVmNull;
import org.cloudbus.cloudsim.gp.vms.GpuVmSimple;
import org.cloudbus.cloudsim.gp.core.AbstractGpu;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.core.GpuResourceStatsComputer;
import org.cloudbus.cloudsim.gp.schedulers.gputask.GpuTaskScheduler;

import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Processor;
import org.cloudbus.cloudsim.schedulers.MipsShare;

import org.cloudsimplus.listeners.EventListener;
import org.gpucloudsimplus.listeners.VGpuGpuEventInfo;
import org.gpucloudsimplus.listeners.VGpuVideocardEventInfo;

import java.util.List;

public interface CustomVGpu extends AbstractGpu, Comparable<CustomVGpu>, 
GpuResourceStatsComputer<VGpuResourceStats> {
	
	CustomVGpu NULL = new CustomVGpuNull ();
	
	//updateProcessing
	double updateGpuTaskProcessing (MipsShare mipsShare);
	
	double updateGpuTaskProcessing (double currentTime, MipsShare mipsShare);
		
	MipsShare getCurrentRequestedMips ();
	
	double getTotalGpuMipsRequested ();
	
	//double getMaxMipsRequested ();
	
	long getCurrentRequestedBw ();
	
	long getCurrentRequestedGddram ();
	
	//utilization in vgpu need
	
	double getTotalGpuMipsUtilization ();

    double getTotalGpuMipsUtilization (double time);
	
	long getId ();
	
	void setId (long id);
	
	void setType (String type);
	
	String getType ();
	
	CustomVGpu setGpuVm (GpuVm gpuVm);
	
	GpuVm getGpuVm ();
	
	CustomVGpu setGpuTaskScheduler (GpuTaskScheduler gpuTaskScheduler);
	
	GpuTaskScheduler getGpuTaskScheduler ();
	
	int getPCIeBw ();
	
	void setPCIeBw (int PCIeBw);
	
	String getTenancy ();
	
	void setTenancy (String tenancy);
	
	///
	String getDescription ();

    CustomVGpu setDescription (String description);

    //VmGroup getGroup ();

    void addStateHistoryEntry (VGpuStateHistoryEntry entry);
    
    long getFreeCoresNumber ();

    long getExpectedFreeCoresNumber ();
    
    CustomVGpu addOnGpuAllocationListener (EventListener<VGpuGpuEventInfo> listener);

    CustomVGpu addOnMigrationStartListener (EventListener<VGpuGpuEventInfo> listener);
    
    CustomVGpu addOnMigrationFinishListener (EventListener<VGpuGpuEventInfo> listener);

    CustomVGpu addOnGpuDeallocationListener (EventListener<VGpuGpuEventInfo> listener);
    
    CustomVGpu addOnCreationFailureListener (EventListener<VGpuVideocardEventInfo> listener);

    CustomVGpu addOnUpdateProcessingListener (EventListener<VGpuGpuEventInfo> listener);
    
    void notifyOnGpuAllocationListeners ();

    void notifyOnGpuDeallocationListeners (Gpu deallocatedGpu);
    
    void notifyOnCreationFailureListeners (Videocard failedVideocard);

    boolean removeOnMigrationStartListener (EventListener<VGpuGpuEventInfo> listener);
    
    boolean removeOnMigrationFinishListener (EventListener<VGpuGpuEventInfo> listener);
    
    boolean removeOnUpdateProcessingListener (EventListener<VGpuGpuEventInfo> listener);
    
    boolean removeOnGpuAllocationListener (EventListener<VGpuGpuEventInfo> listener);

    boolean removeOnGpuDeallocationListener(EventListener<VGpuGpuEventInfo> listener);

    boolean removeOnCreationFailureListener(EventListener<VGpuVideocardEventInfo> listener);

    @Override 
    Resource getBw ();

    @Override
    Resource getGddram ();

    //@Override
    //Resource getStorage ();
    
    List<VGpuStateHistoryEntry> getStateHistory ();

    double getCorePercentUtilization (double time);

    double getCorePercentUtilization ();
    
    double getCorePercentRequested (double time);

    double getCorePercentRequested ();
    
    //void enableUtilizationStats ();

    double getGpuGddramUtilization (); // videocard or Gpu

    double getGpuBwUtilization (); // videocard or Gpu
    
    //videocard's total MIPS capacity
    default double getGpuCoreUtilization () {
        return getGpuCoreUtilization (getSimulation().clock());
    }

    double getGpuCoreUtilization (double time);
    
    double getExpectedGpuCoreUtilization (double vgpuCpuUtilizationPercent);
    
    double getTotalCoreMipsUtilization ();
    
    double getTotalCoreMipsUtilization (double time);
    
    //String getVmm ();
    
    boolean isCreated ();

    boolean isSuitableForGpuTask (GpuTask gpuTask);

    void setCreated (boolean created);
    
    boolean isInMigration ();

    void setInMigration (boolean migrating);

    CustomVGpu setBw (long bwCapacity);

    CustomVGpu setGpu (Gpu gpu);

    CustomVGpu setGddram (long gddramCapacity);

    //CustomVGpu setSize (long size); //storage
    
    void setFailed (boolean failed);

    boolean isFailed ();

    boolean isWorking ();
    
    //@Override
    //default boolean isIdleEnough (final double time) {
    //    return getCloudletScheduler().getCloudletExecList().isEmpty() && AbstractMachine.super.isIdleEnough(time);
    //}
    
    //HorizontalVGpuScaling getHorizontalScaling ();

    //CustomVGpu setHorizontalScaling (HorizontalVGpuScaling horizontalScaling) throws IllegalArgumentException;

    //CustomVGpu setRamVerticalScaling (VerticalVmScaling ramVerticalScaling) throws IllegalArgumentException;

    //Vm setBwVerticalScaling (VerticalVmScaling bwVerticalScaling) throws IllegalArgumentException;

    //Vm setPeVerticalScaling (VerticalVmScaling peVerticalScaling) throws IllegalArgumentException;

    //VerticalVmScaling getRamVerticalScaling ();
    
    //VerticalVmScaling getBwVerticalScaling ();
 
    //VerticalVmScaling getPeVerticalScaling ();

    VGpuCore getVGpuCore ();

    //@Override
    //DatacenterBroker getBroker ();

    //@Override
    //void setBroker (DatacenterBroker broker);
    
    double getStopTime ();
    
    double getTotalExecutionTime ();

    CustomVGpu setStopTime (double stopTime);
    
    //@Override
    //double getTimeZone ();

    //@Override
    //CustomVGpu setTimeZone (double timeZone);
    
    @Override
    Simulation getSimulation ();
    
    Gpu getGpu ();
        
    //getSimulationdouble getMips ();
    
    //long getNumberOfCores ();
    
    double getGpuPercentUtilization (double time);

    double getGpuPercentUtilization ();
    
    CustomVGpu setStartTime (final double startTime);
}
