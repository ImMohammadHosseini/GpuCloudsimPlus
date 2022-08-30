package org.cloudbus.cloudsim.gp.brokers;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GpuDatacenterBroker extends DatacenterBroker {
	Logger LOGGER = LoggerFactory.getLogger(DatacenterBroker.class.getSimpleName());

    GpuDatacenterBroker NULL = new GpuDatacenterBrokerNull();
}
