package org.cloudbus.cloudsim.gp.brokers;

import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;
import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenter;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudlet;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.gp.vms.GpuVm;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public abstract class GpuDatacenterBrokerAbstract extends CloudSimEntity implements 
GpuDatacenterBroker {
	
	private static final Function<GpuVm, Double> DEF_VM_DESTRUCTION_DELAY_FUNC = 
			gpuvm -> DEF_VM_DESTRUCTION_DELAY;

    private boolean selectClosestGpuDatacenter;
    private final List<EventListener<DatacenterBrokerEventInfo>> onGpuVmsCreatedListeners;
    private GpuVm lastSelectedGpuVm;
    private GpuDatacenter lastSelectedDc;
    private double failedGpuVmsRetryDelay;

    private final List<GpuVm> gpuvmFailedList;
    private final List<GpuVm> gpuvmWaitingList;
    private final List<GpuVm> gpuvmExecList;
    private final List<GpuVm> gpuvmCreatedList;

    private final List<GpuCloudlet> gpucloudletWaitingList;
    private final List<GpuCloudlet> gpucloudletSubmittedList;
    private final List<GpuCloudlet> gpucloudletsFinishedList;
    private final List<GpuCloudlet> gpucloudletsCreatedList;

    private boolean wereThereWaitingGpuCloudlets;

    private BiFunction<GpuDatacenter, GpuVm, GpuDatacenter> gpudatacenterMapper;

    private Function<GpuCloudlet, GpuVm> gpuvmMapper;

    private Comparator<GpuVm> gpuvmComparator;
    private Comparator<GpuCloudlet> gpucloudletComparator;

    private int gpuvmCreationRequests;

    private List<GpuDatacenter> gpudatacenterList;

    private GpuCloudlet lastSubmittedGpuCloudlet;
    private GpuVm lastSubmittedGpuVm;

    private Function<GpuVm, Double> gpuvmDestructionDelayFunction;

    private boolean shutdownRequested;
    private boolean shutdownWhenIdle;
    private boolean vmCreationRetrySent;
    
	public GpuDatacenterBrokerAbstract (final CloudSim simulation, final String name) {
		super (simulation);
		if(!name.isEmpty()) {
            setName(name);
        }

        this.onGpuVmsCreatedListeners = new ArrayList<>();
        this.lastSubmittedGpuCloudlet = GpuCloudlet.NULL;
        this.lastSubmittedGpuVm = GpuVm.NULL;
        this.lastSelectedGpuVm = GpuVm.NULL;
        this.lastSelectedDc = GpuDatacenter.NULL;
        this.shutdownWhenIdle = true;

        this.gpuvmCreationRequests = 0;
        this.failedGpuVmsRetryDelay = 5;
        this.gpuvmFailedList = new ArrayList<>();
        this.gpuvmWaitingList = new ArrayList<>();
        this.gpuvmExecList = new ArrayList<>();
        this.gpuvmCreatedList = new ArrayList<>();
        this.gpucloudletWaitingList = new ArrayList<>();
        this.gpucloudletsFinishedList = new ArrayList<>();
        this.gpucloudletsCreatedList = new ArrayList<>();
        this.gpucloudletSubmittedList = new ArrayList<>();
        setGpuDatacenterList(new ArrayList<>());

        setGpuDatacenterMapper(this::defaultGpuDatacenterMapper);
        setGpuVmMapper(this::defaultGpuVmMapper);
        gpuvmDestructionDelayFunction = DEF_VM_DESTRUCTION_DELAY_FUNC;
    }
}
