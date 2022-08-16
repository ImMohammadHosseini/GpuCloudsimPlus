package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.gp.vms.CustomGpuVm;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmNull;
import org.cloudbus.cloudsim.gp.vms.CustomGpuVmSimple;
import org.cloudbus.cloudsim.gp.videocards.Videocard;
import org.cloudbus.cloudsim.gp.schedulers.gputask.GpuTaskScheduler;
import org.cloudbus.cloudsim.gp.schedulers.gputask.GpuTaskSchedulerTimeShared;

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

import static java.util.Objects.requireNonNull;

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
	private VGpuCore vGpuCore;

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
	
    public CustomVGpuSimple(final CustomVGpu sourceVGpu) {
        this(sourceVGpu.getMips(), sourceVGpu.getNumberOfPes());
        this.setBw(sourceVGpu.getBw().getCapacity())
            .setGddram(sourceVGpu.getRam().getCapacity());
            //.setSize(sourceVGpu.getStorage().getCapacity());
    }
    
    public CustomVGpuSimple(final double mips, final long numberOfPes) {
        this(-1, "", "", -1, (long)mips, numberOfPes);
    }
    
    public CustomVGpuSimple(final double mips, final long numberOfPes, 
    		final GpuTaskScheduler gpuTaskScheduler) {
        this(-1, "", "", -1, (long)mips, numberOfPes);
        setGpuTaskScheduler(gpuTaskScheduler);
    }
    
    public CustomVGpuSimple(final long id, final String type, final String tenancy, 
			final int PCIeBw, final double mips, final long numberOfPes) {
        this(id, type, tenancy, PCIeBw,(long)mips, numberOfPes);
    }
    
    //gddram, bw, scheduler, 
	public CustomVGpuSimple (final long id, final String type, final String tenancy, 
			final int PCIeBw, final long mips, final long numberOfPes) {
		setId(id);
		setType(type);
		setTenancy(tenancy);
		setPCIeBw(PCIeBw);
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
        
        this.vGpuCore = new VGpuCore(this, mips, numberOfPes);
        setMips(mips);
        setNumberOfPes(numberOfPes);
        
        mutableAttributesInit();
        
        freePesNumber = numberOfPes;
        expectedFreePesNumber = numberOfPes;
	}
	
	private void mutableAttributesInit () {
        this.description = "";
        this.startTime = -1;
        this.stopTime = -1;
        this.lastBusyTime = Double.MAX_VALUE;
        //setBroker(DatacenterBroker.NULL);
        //setSubmissionDelay(0);
        //setVmm("Xen");

        setInMigration(false);
        this.videocard = Videocard.NULL;
        setGpuTaskScheduler(new GpuTaskSchedulerTimeShared());

        //this.setHorizontalScaling(HorizontalVmScaling.NULL);
        //this.setRamVerticalScaling(VerticalVmScaling.NULL);
        //this.setBwVerticalScaling(VerticalVmScaling.NULL);
        //this.setPeVerticalScaling(VerticalVmScaling.NULL);

        //gpuUtilizationStats = VGpuResourceStats.NULL;

        setRam(new Ram(defaultRamCapacity));
        setBw(new Bandwidth(defaultBwCapacity));
        //setStorage(new SimpleStorage(defaultStorageCapacity));
    }

	@Override
	public double updateGpuTaskProcessing (MipsShare mipsShare) {
		return updateGpuTaskProcessing (getSimulation().clock(), mipsShare);
	}
	
	@Override
	public double updateGpuTaskProcessing (double currentTime, MipsShare mipsShare) {
		requireNonNull(mipsShare);

        if (!gpuTaskScheduler.isEmpty()) {
            setLastBusyTime();
        }
        final double nextSimulationDelay = gpuTaskScheduler.updateProcessing(currentTime, 
        		mipsShare);
        notifyOnUpdateProcessingListeners();

        //gpuUtilizationStats.add(currentTime);
        //getBroker().requestIdleVmDestruction(this);
        if (nextSimulationDelay == Double.MAX_VALUE) {
            return nextSimulationDelay;
        }
        
        final double decimals = currentTime - (int) currentTime;
        return nextSimulationDelay - decimals < 0 ? nextSimulationDelay : nextSimulationDelay - decimals;
	}
	
	@Override
    public long getFreePesNumber () {
        return freePesNumber;
    }
	
	public CustomVGpu setFreePesNumber (long freePesNumber) {
        if (freePesNumber < 0) {
            freePesNumber = 0;
        }
        this.freePesNumber = Math.min (freePesNumber, getNumberOfPes());
        return this;
    }
	
	@Override
    public long getExpectedFreePesNumber () {
        return expectedFreePesNumber;
    }
	
	public CustomVGpu addExpectedFreePesNumber (final long pesToAdd) {
        return setExpectedFreePesNumber (expectedFreePesNumber + pesToAdd);
    }
	
	public CustomVGpu removeExpectedFreePesNumber (final long pesToRemove) {
        return setExpectedFreePesNumber (expectedFreePesNumber - pesToRemove);
    }
	
	private CustomVGpu setExpectedFreePesNumber(long expectedFreePes) {
        if (expectedFreePes < 0) {
            expectedFreePes = 0;
        }
        this.expectedFreePesNumber = expectedFreePes;
        return this;
    }
	
	@Override
	public double getCorePercentUtilization (double time) {
		return gpuTaskScheduler.getAllocatedCpuPercent(time);
	}

	@Override
    public double getCorePercentUtilization () {
		return getCorePercentUtilization(getSimulation().clock());
	}
	
	@Override
	public double getCorePercentRequested (double time) {
		return gpuTaskScheduler.getRequestedCpuPercent(time);
	}

	@Override
    public double getCorePercentRequested () {
		return getCorePercentRequested(getSimulation().clock());
	}
}
