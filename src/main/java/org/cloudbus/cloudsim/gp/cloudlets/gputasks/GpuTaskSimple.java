package org.cloudbus.cloudsim.gp.cloudlets.gputasks;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudletSimple;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.EventListener;

import java.util.List;
import java.util.Set;

public class GpuTaskSimple implements GpuTask {
	
    private long taskId;
    private GpuCloudletSimple gpuCloudlet;
    private long blockLength;
    private long finishedLengthSoFar;
    private long numberOfPes;
    private Status status;
    //private boolean returnedToBroker;
    private double execStartTime;
    //private int priority;
    ///private int netServiceLevel;
    private List<String> requiredFiles;
    private long fileSize;
    private long outputSize;
    private double finishTime;
    private UtilizationModel utilizationModelGpu;
    private UtilizationModel utilizationModelGddram;
    private UtilizationModel utilizationModelBw;

    private final Set<EventListener<CloudletVmEventInfo>> onStartListeners;
    private final Set<EventListener<CloudletVmEventInfo>> onFinishListeners;
    private final Set<EventListener<CloudletVmEventInfo>> onUpdateProcessingListeners;

    private double submissionDelay;
    private double lifeTime;
    //private double arrivalTime;
    
}
