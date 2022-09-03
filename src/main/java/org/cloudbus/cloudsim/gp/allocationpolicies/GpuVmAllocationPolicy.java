package org.cloudbus.cloudsim.gp.allocationpolicies;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
//
public interface GpuVmAllocationPolicy extends VmAllocationPolicy {
    Logger LOGGER = LoggerFactory.getLogger(VmAllocationPolicy.class.getSimpleName());

    GpuVmAllocationPolicy NULL = new GpuVmAllocationPolicyNull();

}
