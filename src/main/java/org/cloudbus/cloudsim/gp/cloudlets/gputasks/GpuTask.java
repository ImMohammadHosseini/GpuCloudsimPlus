package org.cloudbus.cloudsim.gp.cloudlets.gputasks;

import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudlet;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.Simulation;

import java.util.List;


public interface GpuTask {
	
	enum Status {
        INSTANTIATED,
        READY,
        QUEUED,
        FROZEN,
        INEXEC,
        SUCCESS,
        FAILED,
        CANCELED,
        PAUSED,
        RESUMED,
        FAILED_RESOURCE_UNAVAILABLE
    }
	
	GpuTask NULL = new GpuTaskNull();
	
	int NOT_ASSIGNED = -1;
	
	Cloudlet getGpuCloudlet ();
	
	Simulation getSimulation ();
	
	void setGpuCloudlet (GpuCloudlet GpuCloudlet);
	
	boolean addRequiredFile (String fileName);

    boolean addRequiredFiles (List<String> fileNames);

    boolean deleteRequiredFile (String filename);

    boolean hasRequiresFiles ();

    List<String> getRequiredFiles ();

    double getArrivalTime ();
    
    double getActualGpuTime ();

    long getFileSize ();

    long getOutputSize ();

    Status getStatus ();

    //@Override
    //double getSubmissionDelay ();

    //boolean isReturnedToBroker ();

    //double registerArrivalInDatacenter ();
    
    double getExecStartTime ();

    double getFinishTime ();

    long getTaskId ();

    void setTaskId (long taskId);
    
    int getPriority ();
    
    GpuTask setPriority (int priority);

    //int getNetServiceLevel ();

    long getNumberOfCores ();

    UtilizationModel getUtilizationModelBw ();
    
    UtilizationModel getUtilizationModelGpu ();

    UtilizationModel getUtilizationModelGddram ();

    //UtilizationModel getUtilizationModel (Class<? extends ResourceManageable> resourceClass);
    
    double getUtilizationOfGpu ();

    double getUtilizationOfGpu (double time);

    double getUtilizationOfGddram ();
    
    double getUtilizationOfGddram (double time);

    double getUtilizationOfBw ();

    double getUtilizationOfBw (double time);
    
    boolean isBoundToVm ();

    double getWaitingTime ();

    boolean isFinished ();

    GpuTask setFileSize (long fileSize);

    GpuTask setOutputSize (long outputSize);
    
    GpuTask setSizes (long size);

    boolean setStatus (Status newStatus);
    
    //void setNetServiceLevel (int netServiceLevel);

    GpuTask setNumberOfCores (long numberOfCores);
    
    GpuTask setUtilizationModel (UtilizationModel utilizationModel);

    GpuTask setUtilizationModelBw (UtilizationModel utilizationModelBw);

    GpuTask setUtilizationModelGpu (UtilizationModel utilizationModelGpu);

    GpuTask setUtilizationModelGddram (UtilizationModel utilizationModelGddram);

    long getBlockLength ();
    
    GpuTask setBlockLength (long length);

    long getGpuTaskTotalLength ();
    
    long getFinishedLengthSoFar ();

    boolean addFinishedLengthSoFar (long partialFinishedMI);

    void setExecStartTime (double clockTime);
    
    GpuTask addOnStartListener (EventListener<CloudletVmEventInfo> listener);

    boolean removeOnStartListener (EventListener<CloudletVmEventInfo> listener);

    GpuTask addOnUpdateProcessingListener (EventListener<CloudletVmEventInfo> listener);

    boolean removeOnUpdateProcessingListener (EventListener<CloudletVmEventInfo> listener);
    
    GpuTask addOnFinishListener (EventListener<CloudletVmEventInfo> listener);

    boolean removeOnFinishListener (EventListener<CloudletVmEventInfo> listener);

    void notifyOnUpdateProcessingListeners (double time);

    //@Override
    //DatacenterBroker getBroker ();

    //@Override
    //void setBroker (DatacenterBroker broker);

    GpuTask reset ();

    GpuTask setLifeTime (double lifeTime);

	double getLifeTime ();
}
