package org.cloudbus.cloudsim.gp.cloudlets;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.core.CustomerEntity;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.vms.Vm;

import org.cloudbus.cloudsim.gp.cloudlets.gputasks.GpuTask;
import org.cloudbus.cloudsim.gp.vms.GpuVm;

import java.util.*;


public class GpuCloudletNull implements GpuCloudlet {
	
	@Override public void setId(long id) {/**/}
    @Override public long getId() {
        return -1;
    }
    @Override public String getUid() {
        return "";
    }
    @Override public boolean addRequiredFile(String fileName) {
        return false;
    }
    @Override public boolean addRequiredFiles(List<String> fileNames) {
        return false;
    }
    @Override public boolean deleteRequiredFile(String filename) {
        return false;
    }
    @Override public double getArrivalTime() { return -1; }
    @Override public double getActualCpuTime() {
        return 0.0;
    }
    @Override public int getPriority() {
        return 0;
    }
    @Override public long getFileSize() {
        return 0L;
    }
    @Override public long getFinishedLengthSoFar() {
        return 0L;
    }
    @Override public long getLength() {
        return 0L;
    }
    @Override public long getOutputSize() {
        return 0L;
    }
    @Override public long getTotalLength() {
        return 0L;
    }
    @Override public double getExecStartTime() {
        return 0.0;
    }
    @Override public double getFinishTime() {
        return 0.0;
    }
    @Override public int getNetServiceLevel() {
        return 0;
    }
    @Override public long getNumberOfPes() {
        return 0;
    }
    @Override public List<String> getRequiredFiles() {
        return Collections.emptyList();
    }
    @Override public Status getStatus() {
        return Status.FAILED;
    }
    @Override public boolean isReturnedToBroker() { return false; }
    @Override public long getJobId() { return 0; }
    @Override public void setJobId(long jobId) {/**/}
    @Override public UtilizationModel getUtilizationModelBw() {
        return UtilizationModel.NULL;
    }
    @Override public UtilizationModel getUtilizationModelCpu() {
        return UtilizationModel.NULL;
    }
    @Override public UtilizationModel getUtilizationModelRam() {
        return UtilizationModel.NULL;
    }
    @Override public UtilizationModel getUtilizationModel (
    		Class<? extends ResourceManageable> resourceClass) { return UtilizationModel.NULL; }
    @Override public double getUtilizationOfBw() {
        return 0;
    }
    @Override public double getUtilizationOfBw(double time) {
        return 0.0;
    }
    @Override public double getUtilizationOfCpu() {
        return 0;
    }
    @Override public double getUtilizationOfCpu(double time) {
        return 0.0;
    }
    @Override public double getUtilizationOfRam() {
        return 0;
    }
    @Override public double getUtilizationOfRam(double time) {
        return 0.0;
    }
    @Override public GpuVm getVm() {
        return GpuVm.NULL;
    }
    @Override public double getWaitingTime() {
        return 0.0;
    }
    @Override public boolean isFinished() {
        return false;
    }
    @Override public boolean hasRequiresFiles() {
        return false;
    }
    @Override public GpuCloudlet setPriority(int priority) { return this; }
    @Override public GpuCloudlet setLength(long length) {
        return GpuCloudlet.NULL;
    }
    @Override public GpuCloudlet setFileSize(long fileSize) {
        return GpuCloudlet.NULL;
    }
    @Override public GpuCloudlet setOutputSize(long outputSize) {
        return GpuCloudlet.NULL;
    }
    @Override public GpuCloudlet setSizes(long size) { return this; }
    @Override public boolean setStatus(Status newStatus) {
        return false;
    }
    @Override public void setNetServiceLevel(int netServiceLevel) {/**/}
    @Override public GpuCloudlet setNumberOfPes(long numberOfPes) {
        return GpuCloudlet.NULL;
    }
    @Override public void setBroker(DatacenterBroker broker) {/**/}
    @Override public DatacenterBroker getBroker() {
        return DatacenterBroker.NULL;
    }
    @Override public GpuCloudlet setUtilizationModel(UtilizationModel utilizationModel) {
        return GpuCloudlet.NULL;
    }
    @Override public GpuCloudlet setUtilizationModelBw(UtilizationModel utilizationModelBw) {
        return GpuCloudlet.NULL;
    }
    @Override public GpuCloudlet setUtilizationModelCpu(UtilizationModel utilizationModelCpu) {
        return GpuCloudlet.NULL;
    }
    @Override public GpuCloudlet setUtilizationModelRam(UtilizationModel utilizationModelRam) {
        return GpuCloudlet.NULL;
    }
    @Override public GpuCloudlet setVm(Vm vm) {
        return GpuCloudlet.NULL;
    }
    @Override public boolean removeOnFinishListener(EventListener<CloudletVmEventInfo> listener) { 
    	return false; }
    @Override public GpuCloudlet addOnFinishListener(EventListener<CloudletVmEventInfo> listener) { 
    	return GpuCloudlet.NULL; }
    @Override public void notifyOnUpdateProcessingListeners(double time) {/**/}
    @Override public Simulation getSimulation() {
        return Simulation.NULL;
    }
    @Override public void setLastTriedDatacenter(Datacenter lastTriedDatacenter) {/**/}
    @Override public Datacenter getLastTriedDatacenter() { return Datacenter.NULL; }
    @Override public double getArrivedTime() { return 0; }
    @Override public CustomerEntity setArrivedTime(double time) { return this; }
    @Override public double getCreationTime() { return 0; }
    @Override public double getWaitTime() { return 0; }
    @Override public boolean removeOnUpdateProcessingListener(
    		EventListener<CloudletVmEventInfo> listener) { return false; }
    @Override public GpuCloudlet addOnUpdateProcessingListener(
    		EventListener<CloudletVmEventInfo> listener) { return GpuCloudlet.NULL; }
    @Override public double getSubmissionDelay() { return 0; }
    @Override public boolean isDelayed() { return false; }
    @Override public void setSubmissionDelay(double submissionDelay) {/**/}
    @Override public boolean isBoundToVm() { return false; }
    @Override public int compareTo(Cloudlet cloudlet) {
        return 0;
    }
    @Override public String toString() {
        return "Cloudlet.NULL";
    }
    @Override public boolean addFinishedLengthSoFar(long partialFinishedMI) {
        return false;
    }
    @Override public void setExecStartTime(double clockTime) {/**/}
    @Override public GpuCloudlet addOnStartListener(EventListener<CloudletVmEventInfo> listener) { 
    	return this; }
    @Override public boolean removeOnStartListener(EventListener<CloudletVmEventInfo> listener) { 
    	return false; }
    @Override public double registerArrivalInDatacenter() {
        return -1;
    }
    @Override public GpuCloudlet reset() { return this; }
    @Override public GpuCloudlet setLifeTime(final double lifeTime) { return this; }
    @Override public double getLifeTime() { return -1; }
    @Override public GpuCloudlet setGpuTask (GpuTask gpuTask) { return this; }
	@Override public GpuTask getGpuTask () { return GpuTask.NULL; }
	
}
