package org.cloudbus.cloudsim.gp.schedulers.gpucloudlet;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.schedulers.cloudlet.network.CloudletTaskScheduler;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudlet;

import org.cloudsimplus.listeners.CloudletResourceAllocationFailEventInfo;
import org.cloudsimplus.listeners.EventListener;

import java.io.Serial;
import java.util.*;

public abstract class GpuCloudletSchedulerAbstract implements GpuCloudletScheduler {
	
	@Serial
    private static final long serialVersionUID = -2314361120790372742L;

    private final List<CloudletExecution> cloudletPausedList;
    private final List<CloudletExecution> cloudletFinishedList;
    private final List<CloudletExecution> cloudletFailedList;
    private final List<CloudletExecution> cloudletExecList;
    private final List<CloudletExecution> cloudletWaitingList;

    private CloudletTaskScheduler taskScheduler;

    private double previousTime;

    private MipsShare currentMipsShare;
    private boolean enableCloudletSubmittedList;
    private final List<GpuCloudlet> cloudletSubmittedList;
    private final Set<GpuCloudlet> cloudletReturnedList;

    private GpuVm gpuVm;

    private final List<EventListener<CloudletResourceAllocationFailEventInfo>> resourceAllocationFailListeners;

    protected GpuCloudletSchedulerAbstract () {
        setPreviousTime(0.0);
        gpuVm = GpuVm.NULL;
        cloudletSubmittedList = new ArrayList<>();
        cloudletExecList = new ArrayList<>();
        cloudletPausedList = new ArrayList<>();
        cloudletFinishedList = new ArrayList<>();
        cloudletFailedList = new ArrayList<>();
        cloudletWaitingList = new ArrayList<>();
        cloudletReturnedList = new HashSet<>();
        currentMipsShare = new MipsShare();
        taskScheduler = CloudletTaskScheduler.NULL;
        resourceAllocationFailListeners = new ArrayList<>();
    }
    
    
    
    
    
    
    @Override
    public List<Cloudlet> getCloudletCreatedList () {
    	return getGpuCloudletCreatedList();
    }
    
    public <T extends Cloudlet> List<T> getGpuCloudletCreatedList () {
    	return (List<T>) gpucloudletsCreatedList;
    }
}
