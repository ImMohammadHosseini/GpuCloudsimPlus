package org.cloudbus.cloudsim.gp.brokers;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

import org.cloudbus.cloudsim.gp.datacenters.GpuDatacenter;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;
import org.cloudbus.cloudsim.gp.cloudlets.GpuCloudlet;
import org.cloudbus.cloudsim.gp.vms.GpuVm;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.EventListener;

public class GpuDatacenterBrokerSimple extends GpuDatacenterBrokerAbstract {
	
	private int lastSelectedGpuVmIndex;
    private int lastSelectedGpuDcIndex;

    public GpuDatacenterBrokerSimple (final CloudSim simulation) {
        this(simulation, "");
    }

    public GpuDatacenterBrokerSimple (final CloudSim simulation, final String name) {
        super(simulation, name);
        this.lastSelectedGpuVmIndex = -1;
        this.lastSelectedGpuDcIndex = -1;
    }

    @Override
    protected GpuDatacenter defaultGpuDatacenterMapper(final GpuDatacenter lastDatacenter, 
    		final GpuVm vm) {
        if(getGpuDatacenterList().isEmpty()) {
            throw new IllegalStateException("You don't have any Datacenter created.");
        }

        if (lastDatacenter != GpuDatacenter.NULL) {
            return getGpuDatacenterList().get(lastSelectedGpuDcIndex);
        }

        
        if(lastSelectedGpuDcIndex == getGpuDatacenterList().size()-1){
            return GpuDatacenter.NULL;
        }

        return getGpuDatacenterList().get(++lastSelectedGpuDcIndex);
    }

    @Override
    protected GpuVm defaultGpuVmMapper(final GpuCloudlet cloudlet) {
        if (cloudlet.isBoundToVm()) {
            return (GpuVm)cloudlet.getVm();
        }

        if (getVmExecList().isEmpty()) {
            return GpuVm.NULL;
        }

        lastSelectedGpuVmIndex = ++lastSelectedGpuVmIndex % getVmExecList().size();
        return getGpuVmFromCreatedList(lastSelectedGpuVmIndex);
    }

}
