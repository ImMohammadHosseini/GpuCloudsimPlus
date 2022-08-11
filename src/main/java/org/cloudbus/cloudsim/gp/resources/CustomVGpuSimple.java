package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.vms.CustomGpuVm;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmNull;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmSimple;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Processor;
import org.cloudbus.cloudsim.resources.ResourceManageable;

import java.util.List;

public class CustomVGpuSimple implements CustomVGpu {

	//private long id;
	private String type;
	private String tenancy;
	private boolean inMigration;
	private int PCIeBw;
	
	private CustomGpuVmSimple gpuVm;
	
	private Processor vGpuProcessors;
	private Ram gddram;
	private Bandwidth bw;
	
	private long freePesNumber;
    private long expectedFreePesNumber;
	
	private final List<VGpuStateHistoryEntry> vGpuStateHistory;
	
	//private VGpuResourceStats gpuUtilizationStats;
	//private HorizontalVmScaling horizontalScaling;
	
    private boolean failed;
    private boolean created;
    
    private List<ResourceManageable> resources;

    private GpuTaskSchduler gpuTaskSchduler;
    
    //private double submissionDelay;
    
    /*private final List<EventListener<VmHostEventInfo>> onMigrationStartListeners;
    private final List<EventListener<VmHostEventInfo>> onMigrationFinishListeners;
    private final List<EventListener<VmHostEventInfo>> onHostAllocationListeners;
    private final List<EventListener<VmHostEventInfo>> onHostDeallocationListeners;
    private final List<EventListener<VmHostEventInfo>> onUpdateProcessingListeners;
    private final List<EventListener<VmDatacenterEventInfo>> onCreationFailureListeners;

    private VerticalVmScaling ramVerticalScaling;
    private VerticalVmScaling bwVerticalScaling;
    private VerticalVmScaling peVerticalScaling;

    private String description;
    private double startTime;
    private double stopTime;
    private double lastBusyTime;
    private VmGroup group;
    private double timeZone;
    private MipsShare allocatedMips;
    private MipsShare requestedMips;*/
	
	public CustomVGpu () {
		
	}
}
