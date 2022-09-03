package org.cloudbus.cloudsim.gp.datacenters;

import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.resources.SanStorage;
import org.cloudbus.cloudsim.resources.DatacenterStorage;
import org.cloudbus.cloudsim.power.models.PowerModelDatacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.power.models.PowerModelDatacenterSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristicsSimple;

import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.gp.hosts.GpuHost;
import org.cloudbus.cloudsim.gp.hosts.GpuHostSimple;
import org.cloudbus.cloudsim.gp.allocationpolicies.GpuVmAllocationPolicy;
import org.cloudbus.cloudsim.gp.allocationpolicies.GpuVmAllocationPolicySimple;

import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.HostEventInfo;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.cloudbus.cloudsim.util.BytesConversion.bitesToBytes;

public class GpuDatacenterSimple extends CloudSimEntity implements GpuDatacenter {

    private double lastUnderOrOverloadedDetection = -Double.MAX_VALUE;
    private double bandwidthPercentForMigration;

    private boolean migrationsEnabled;

    private List<? extends GpuHost> gpuhostList;
    private final DatacenterCharacteristics characteristics;
    private GpuVmAllocationPolicy gpuVmAllocationPolicy;

    private double lastProcessTime;
    private double schedulingInterval;
    
	private DatacenterStorage datacenterStorage;

    private final List<EventListener<HostEventInfo>> onGpuHostAvailableListeners;
    private final List<EventListener<DatacenterVmMigrationEventInfo>> onVmMigrationFinishListeners;

    private double timeZone;
    private Map<GpuVm, GpuHost> lastMigrationMap;

    private double gpuhostSearchRetryDelay;

    private PowerModelDatacenter powerModel = PowerModelDatacenter.NULL;
    private long activeHostsNumber;

    public GpuDatacenterSimple (final Simulation simulation, 
    		final List<? extends GpuHost> hostList) {
        this(simulation, hostList, new GpuVmAllocationPolicySimple(), new DatacenterStorage());
    }

    public GpuDatacenterSimple (final Simulation simulation, final List<? extends GpuHost> hostList,
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy) {
    	this(simulation, hostList, gpuVmAllocationPolicy, new DatacenterStorage());
    }

    public GpuDatacenterSimple ( final Simulation simulation, 
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy) {
        this(simulation, new ArrayList<>(), gpuVmAllocationPolicy, new DatacenterStorage());
    }

    public GpuDatacenterSimple (final Simulation simulation, 
    		final List<? extends GpuHost> hostList,
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy,final List<SanStorage> storageList) {
        this(simulation, hostList, gpuVmAllocationPolicy, new DatacenterStorage(storageList));
    }
    
    public GpuDatacenterSimple (final Simulation simulation, final List<? extends GpuHost> hostList,
    		final GpuVmAllocationPolicy gpuVmAllocationPolicy, final DatacenterStorage storage) {
        super(simulation);
        setHostList(hostList);
        setLastProcessTime(0.0);
        setSchedulingInterval(0);
        setDatacenterStorage(storage);
        setPowerModel(new PowerModelDatacenterSimple(this));

        this.onGpuHostAvailableListeners = new ArrayList<>();
        this.onVmMigrationFinishListeners = new ArrayList<>();
        this.characteristics = new DatacenterCharacteristicsSimple(this);
        this.bandwidthPercentForMigration = DEF_BW_PERCENT_FOR_MIGRATION;
        this.migrationsEnabled = true;
        this.gpuhostSearchRetryDelay = -1;

        this.lastMigrationMap = Collections.emptyMap();

        setVmAllocationPolicy(gpuVmAllocationPolicy);
    }
    
    private void setHostList (final List<? extends GpuHost> hostList) {
        this.gpuhostList = requireNonNull(hostList);
        setupGpuHosts();
    }
    
    private void setupGpuHosts () {
        long lastGpuHostId = getLastGpuHostId();
        for (final GpuHost host : gpuhostList) {
            lastGpuHostId = setupGpuHost (host, lastGpuHostId);
        }
    }
    
    private long getLastGpuHostId () {
        return gpuhostList.isEmpty() ? -1 : gpuhostList.get(gpuhostList.size()-1).getId();
    }
    
//need review
    protected long setupGpuHost (final GpuHost host, long nextId) {
        nextId = Math.max(nextId, -1);
        if(host.getId() < 0) {
            host.setId(++nextId);
        }

        host.setSimulation(getSimulation()).setDatacenter(this);
        host.setActive(((GpuHostSimple)host).isActivateOnDatacenterStartup());
        return nextId;
    }
}
