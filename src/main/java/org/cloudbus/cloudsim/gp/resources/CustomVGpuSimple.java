package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.vms.CustomGpuVm;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmNull;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmSimple;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.schedulers.gputask.GpuTaskScheduler;

import org.gpucloudsimplus.listeners.VGpuVideocardEventInfo;
import org.cloudsimplus.listeners.EventListener;

import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Processor;
import org.cloudbus.cloudsim.schedulers.MipsShare;
import org.cloudbus.cloudsim.resources.ResourceManageable;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class CustomVGpuSimple implements CustomVGpu {

	private static long defaultRamCapacity = 1024;
    private static long defaultBwCapacity = 100;
    
	private long id;
	private String type;
	private String tenancy;
	private int PCIeBw;
	
	private CustomGpuVm gpuVm;
	
	private Ram gddram;
	private Bandwidth bw;
	private Processor vGpuProcessors;

	private Videocard videocard;
	
	private long freePesNumber;
    private long expectedFreePesNumber;
	
	private final List<VGpuStateHistoryEntry> vGpuStateHistory;
	
	//private VGpuResourceStats gpuUtilizationStats;
	//private HorizontalVmScaling horizontalScaling;
	
    private boolean failed;
    private boolean created;
	private boolean inMigration;

    private List<ResourceManageable> resources;

    private GpuTaskScheduler gpuTaskScheduler;
    
    //private double submissionDelay;
    
    private final List<EventListener<VGpuVideocardEventInfo>> onMigrationStartListeners;
    private final List<EventListener<VGpuVideocardEventInfo>> onMigrationFinishListeners;
    private final List<EventListener<VGpuVideocardEventInfo>> onVideocardAllocationListeners;
    private final List<EventListener<VGpuVideocardEventInfo>> onVideocardDeallocationListeners;
    private final List<EventListener<VGpuVideocardEventInfo>> onUpdateProcessingListeners;
    
    /*private final List<EventListener<VmDatacenterEventInfo>> onCreationFailureListeners;

    private VerticalVmScaling ramVerticalScaling;
    private VerticalVmScaling bwVerticalScaling;
    private VerticalVmScaling peVerticalScaling;*/

    private String description;
    private double startTime;
    private double stopTime;
    private double lastBusyTime;
    //private VmGroup group;
    private double timeZone;
    private MipsShare allocatedMips;
    private MipsShare requestedMips;
	
    
    
    //gddram, bw, type,tenancy, scheduler, pciew
	public CustomVGpuSimple (final long id, final long mipsCapacity, final long numberOfPes) {
		setId(id);
        this.resources = new ArrayList<>(4);
        this.onMigrationStartListeners = new ArrayList<>();
        this.onMigrationFinishListeners = new ArrayList<>();
        this.onVideocardAllocationListeners = new ArrayList<>();
        this.onVideocardDeallocationListeners = new ArrayList<>();
        //this.onCreationFailureListeners = new ArrayList<>();
        this.onUpdateProcessingListeners = new ArrayList<>();
        this.vGpuStateHistory = new LinkedList<>();
        this.allocatedMips = new MipsShare();
        this.requestedMips = new MipsShare();
        
        this.vGpuProcessors = new Processor(this, mipsCapacity, numberOfPes);
        setMips(mipsCapacity);
        setNumberOfPes(numberOfPes);
        
        mutableAttributesInit();
        
        freePesNumber = numberOfPes;
        expectedFreePesNumber = numberOfPes;
	}
	
	
}
