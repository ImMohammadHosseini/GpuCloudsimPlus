package org.cloudbus.cloudsim.gp.datacenters;

import org.cloudbus.cloudsim.datacenters.Datacenter;

import org.cloudbus.cloudsim.gp.allocationpolicies.GpuVmAllocationPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Stream;

public interface GpuDatacenter extends Datacenter {
	Logger LOGGER = LoggerFactory.getLogger(GpuDatacenter.class.getSimpleName());

    GpuDatacenter NULL = new GpuDatacenterNull();
	
    @Override
    GpuVmAllocationPolicy getVmAllocationPolicy();
}
