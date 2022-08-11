package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.vms.CustomGpuVm;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmNull;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmSimple;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Processor;

import java.util.List;

public class CustomVGpuSimple implements CustomVGpu {

	private long id;
	private String type;
	private String tenancy;
	private boolean inMigration;
	
	private CustomGpuVmSimple gpuVm;
	
	private Processor vGpuProcessors;
	private Ram gddram;
	private Bandwidth bw;
	
	private final List<VGpuStateHistoryEntry> vGpuStateHistory;
	
	public CustomVGpu () {
		
	}
}
