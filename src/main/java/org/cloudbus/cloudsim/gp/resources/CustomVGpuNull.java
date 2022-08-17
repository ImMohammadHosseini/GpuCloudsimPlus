package org.cloudbus.cloudsim.gp.resources;


import org.cloudbus.cloudsim.gp.vms.CustomGpuVm;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.schedulers.gputask.GpuTaskScheduler;

import org.gpucloudsimplus.listeners.VGpuVideocardEventInfo;
import org.cloudsimplus.listeners.EventListener;

import org.cloudbus.cloudsim.resources.Resource;
import org.cloudbus.cloudsim.schedulers.MipsShare;

import java.util.*;

public class CustomVGpuNull implements CustomVGpu {
	
	@Override public void setId (long id) {/**/}
    @Override public long getId () {
        return -1;
    }
    @Override public void addStateHistoryEntry (VGpuStateHistoryEntry entry) {/**/}
    @Override public Resource getBw () {
        return Resource.NULL;
    }
    @Override public GpuTaskScheduler getGpuTaskScheduler () { return GpuTaskScheduler.NULL; }
    @Override public long getFreePesNumber () { return 0; }
    @Override public long getExpectedFreePesNumber () { return 0; }
    @Override public long getCurrentRequestedBw () {
        return 0;
    }
    @Override public MipsShare getCurrentRequestedMips () {
        return MipsShare.NULL;
    }
    @Override public long getCurrentRequestedGddram () {
        return 0;
    }
    @Override public double getTotalCoreMipsRequested () {
        return 0.0;
    }
    @Override public Videocard getVideocard () {
        return Videocard.NULL;
    }
    @Override public double getMips () {
        return 0;
    }
    @Override public long getNumberOfPes () {
        return 0;
    }
    @Override public CustomVGpu addOnVideocardAllocationListener (
    		EventListener<VGpuVideocardEventInfo> listener) {
        return this;
    }
    @Override public CustomVGpu addOnMigrationStartListener (
    		EventListener<VGpuVideocardEventInfo> listener) { return this; }
    @Override public CustomVGpu addOnMigrationFinishListener (
    		EventListener<VGpuVideocardEventInfo> listener) { return this; }
    @Override public CustomVGpu addOnVideocardDeallocationListener (
    		EventListener<VGpuVideocardEventInfo> listener) { return this; }
    
    @Override public CustomVGpu addOnUpdateProcessingListener (
    		EventListener<VGpuVideocardEventInfo> listener) {
        return this;
    }
    @Override public void notifyOnVideocardAllocationListeners () {/**/}
    @Override public void notifyOnVideocardDeallocationListeners (
    		Videocard deallocatedVideocard) {/**/}
    @Override public boolean removeOnMigrationStartListener (
    		EventListener<VGpuVideocardEventInfo> listener) { return false; }
    @Override public boolean removeOnMigrationFinishListener (
    		EventListener<VGpuVideocardEventInfo> listener) { return false; }
    @Override public boolean removeOnUpdateProcessingListener (
    		EventListener<VGpuVideocardEventInfo> listener) { return false; 
    		}
    @Override public boolean removeOnVideocardAllocationListener (
    		EventListener<VGpuVideocardEventInfo> listener) { return false; }
    
    @Override public boolean removeOnVideocardDeallocationListener (
    		EventListener<VGpuVideocardEventInfo> listener) {
        return false;
    }
    @Override public Resource getGddram () {
        return Resource.NULL;
    }
    
    @Override public List<VGpuStateHistoryEntry> getStateHistory () {
        return Collections.emptyList();
    }
    @Override public double getCorePercentUtilization () { return 0; }
    @Override public double getCorePercentUtilization (double time) {
        return 0.0;
    }
    @Override public double getCorePercentRequested () { return 0; }
    @Override public double getCorePercentRequested (double time) { return 0; }
    @Override public double getVideocardCoreUtilization (double time) { return 0; }
    @Override public double getExpectedVideocardCoreUtilization (double vmCpuUtilizationPercent) { 
    	return 0; 
    }
    @Override public double getVideocardGddramUtilization () { return 0; }
    @Override public double getVideocardBwUtilization () { return 0; }
    @Override public double getTotalCoreMipsUtilization () { return 0; }
    @Override public double getTotalCoreMipsUtilization (double time) {
        return 0.0;
    }
    
    @Override public double getStopTime () { return 0; }
    @Override public double getTotalExecutionTime () { return 0; }
    @Override public CustomVGpu setStopTime (double stopTime) { return this; }
    @Override public boolean isCreated () {
        return false;
    }
    @Override public boolean isSuitableForGpuTask (GpuTask gpuTask) { return false; }
    @Override public boolean isInMigration () {
        return false;
    }
    @Override public void setCreated (boolean created) {/**/}
    @Override public CustomVGpu setBw (long bwCapacity) {
        return this;
    }
    @Override public CustomVGpu setVideocard (Videocard videocard) { return this; }
    @Override public void setInMigration (boolean migrating) {/**/}
    @Override public CustomVGpu setGddram (long ram) {
        return this;
    }
    
    @Override public double updateGpuTaskProcessing (double currentTime, MipsShare mipsShare) { 
    	return 0.0; 
    }
    @Override public double updateGpuTaskProcessing (MipsShare mipsShare) { return 0; }
    @Override public CustomVGpu setGpuTaskScheduler(GpuTaskScheduler gpuTaskScheduler) {
        return this;
    }
    
    @Override public void setFailed(boolean failed) {/**/}
    @Override public boolean isFailed() {
        return true;
    }
    @Override public boolean isWorking() { return false; }
    @Override public Simulation getSimulation() {
        return Simulation.NULL;
    }
    @Override public String toString() { return "Vm.NULL"; }
    
    @Override public VGpuCore getVGpuCore() { return VGpuCore.NULL; }
    @Override public String getDescription() { return ""; }
    @Override public CustomVGpu setDescription (String description) { return this; }
    @Override public String getType () { return ""; }
    @Override public void setType (String type) { /**/ }
    @Override public CustomGpuVm getGpuVm () { return CustomGpuVm.NULL; } 
    @Override public CustomVGpu setGpuVm (CustomGpuVm gpuVm) { return this; } 
    @Override public int getPCIeBw () {
    	return -1;
    }
    @Override public void setPCIeBw (int PCIeBw) { /**/ } 
    @Override public String getTenancy () { return ""; } 
    @Override public void setTenancy (String tenancy) { /**/ }
    
}

