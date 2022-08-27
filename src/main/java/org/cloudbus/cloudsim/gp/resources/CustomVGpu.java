package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.vms.CustomGpuVm;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmNull;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmSimple;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.schedulers.gputask.GpuTaskScheduler;

import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Processor;
import org.cloudbus.cloudsim.schedulers.MipsShare;

import org.cloudsimplus.listeners.EventListener;
import org.gpucloudsimplus.listeners.VGpuVideocardEventInfo;

import java.util.List;

public interface CustomVGpu {
	
	CustomVGpu NULL = new CustomVGpuNull ();
	
	//updateProcessing
	double updateGpuTaskProcessing (MipsShare mipsShare);
	
	double updateGpuTaskProcessing (double currentTime, MipsShare mipsShare);
		
	MipsShare getCurrentRequestedMips ();
	
	//getTotalCpuMipsRequested
	double getTotalCoreMipsRequested ();
	
	//double getMaxMipsRequested ();
	
	long getCurrentRequestedBw();
	
	long getCurrentRequestedGddram();
	
	//utilization in vgpu need
	
	long getId ();
	
	void setId (long id);
	
	void setType (String type);
	
	String getType ();
	
	CustomVGpu setGpuVm (CustomGpuVm gpuVm);
	
	CustomGpuVm getGpuVm ();
	
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
    
    CustomVGpu addOnVideocardAllocationListener (EventListener<VGpuVideocardEventInfo> listener);

    CustomVGpu addOnMigrationStartListener (EventListener<VGpuVideocardEventInfo> listener);
    
    CustomVGpu addOnMigrationFinishListener (EventListener<VGpuVideocardEventInfo> listener);

    CustomVGpu addOnVideocardDeallocationListener (EventListener<VGpuVideocardEventInfo> listener);
    
    //CustomVGpu addOnCreationFailureListener (EventListener<VmDatacenterEventInfo> listener);

    CustomVGpu addOnUpdateProcessingListener (EventListener<VGpuVideocardEventInfo> listener);
    
    void notifyOnVideocardAllocationListeners ();

    void notifyOnGpuDeallocationListeners (Gpu deallocatedGpu);
    
    //void notifyOnCreationFailureListeners (Datacenter failedDatacenter);

    boolean removeOnMigrationStartListener (EventListener<VGpuVideocardEventInfo> listener);
    
    boolean removeOnMigrationFinishListener (EventListener<VGpuVideocardEventInfo> listener);
    
    boolean removeOnUpdateProcessingListener (EventListener<VGpuVideocardEventInfo> listener);
    
    boolean removeOnVideocardAllocationListener (EventListener<VGpuVideocardEventInfo> listener);

    boolean removeOnVideocardDeallocationListener(EventListener<VGpuVideocardEventInfo> listener);

    //boolean removeOnCreationFailureListener(EventListener<VmDatacenterEventInfo> listener);

    //@Override AbstractMachine
    Resource getBw ();

    //@Override
    Resource getGddram ();

    //@Override
    //Resource getStorage ();
    
    List<VGpuStateHistoryEntry> getStateHistory ();

    double getCorePercentUtilization (double time);

    double getCorePercentUtilization ();
    
    double getCorePercentRequested (double time);

    double getCorePercentRequested ();
    
    //void enableUtilizationStats ();

    double getVideocardGddramUtilization (); // videocard or Gpu

    double getVideocardBwUtilization (); // videocard or Gpu
    
    //videocard's total MIPS capacity
    default double getVideocardCoreUtilization () {
        return getVideocardCoreUtilization (getSimulation().clock());
    }

    double getVideocardCoreUtilization (double time);
    
    double getExpectedVideocardCoreUtilization (double vgpuCpuUtilizationPercent);
    
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
    
    Simulation getSimulation ();
    
    Gpu getGpu ();
        
    double getMips ();
    
    long getNumberOfCores ();
    
    double getGpuPercentUtilization (double time);

    double getGpuPercentUtilization ();
    
    CustomVGpu setStartTime (final double startTime);
}
