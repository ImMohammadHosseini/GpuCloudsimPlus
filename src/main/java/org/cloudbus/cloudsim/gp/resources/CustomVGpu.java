package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.vms.CustomGpuVm;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmNull;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmSimple;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Processor;

import java.util.List;

public interface CustomVGpu {
	
	CustomVGpu NULL = new CustomVGpuNull ();
	
	//updateProcessing
	double updateGpuTaskProcessing (double currentTime, MipsShare mipsShare);
	
	MipsShare getCurrentRequestedMips ();
	
	//getTotalCpuMipsRequested
	double getTotalMipsRequested ();
	
	//double getMaxMipsRequested ();
	
	long getCurrentRequestedBw();
	
	long getCurrentRequestedGddram();
	
	//utilization in vgpu need
	
	//long getId ();
	
	//void setId (long id);
	
	void setType (String type);
	
	String getType ();
	
	CustomVGpu setGpuVm (CustomGpuVm gpuVm);
	
	CustomGpuVm getGpuVm ();
	
	CustomVGpu setGpuTaskScheduler (GpuTaskScheduler gpuTaskScheduler);
	
	GpuTaskScheduler getGpuTaskScheduler ();
	
	int getPCIeBw ();
	
	void setPCIeBw (int PCIeBw);
	
	String getTenancy ();
	
	void setTenancy ();
	
	///
	//String getDescription();

    //CustomVGpu setDescription(String description);

    //VmGroup getGroup();

    void addStateHistoryEntry(VGpuStateHistoryEntry entry);
    
    long getFreePesNumber();

    long getExpectedFreePesNumber();
}
