package org.cloudbus.cloudsim.gp.brokers;

import org.cloudbus.cloudsim.core.SimEntityNullBase;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.vms.Vm;

import org.cloudsimplus.listeners.DatacenterBrokerEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.EventInfo;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.Collections;

final class GpuDatacenterBrokerNull implements GpuDatacenterBroker, SimEntityNullBase {
	
	@Override public int compareTo (SimEntity entity) { return 0; }
    @Override public boolean bindCloudletToVm (Cloudlet cloudlet, Vm vm) {
        return false;
    }
    @Override public <T extends Cloudlet> List<T> getCloudletWaitingList () {
        return Collections.emptyList();
    }
    @Override public <T extends Cloudlet> List<T> getCloudletFinishedList () {
        return Collections.emptyList();
    }
    @Override public Vm getWaitingVm (int index) {
        return Vm.NULL;
    }
    @Override public <T extends Vm> List<T> getVmWaitingList () {
        return Collections.emptyList();
    }
    @Override public <T extends Vm> List<T> getVmExecList () {
        return Collections.emptyList();
    }
    @Override public int getVmsNumber () { return 0; }
    @Override public GpuDatacenterBroker requestIdleVmDestruction (Vm vm) { return this; }
    @Override public void requestShutdownWhenIdle () {/**/}
    @Override public List<Cloudlet> destroyVm (Vm vm) { return Collections.emptyList(); }
    @Override public <T extends Vm> List<T> getVmCreatedList () { return Collections.emptyList(); }
    @Override public GpuDatacenterBroker setDatacenterMapper (
    		BiFunction<Datacenter, Vm, Datacenter> datacenterMapper) { return this; }
    @Override public GpuDatacenterBroker setVmMapper (Function<Cloudlet, Vm> vmMapper) { return this; }
    @Override public GpuDatacenterBroker setSelectClosestDatacenter (boolean select) { return this; }
    @Override public boolean isSelectClosestDatacenter () { return false; }
    @Override public List<Cloudlet> getCloudletCreatedList () { return Collections.emptyList(); }
    @Override public GpuDatacenterBroker addOnVmsCreatedListener (
    		EventListener<DatacenterBrokerEventInfo> listener) { return this; }
    @Override public GpuDatacenterBroker removeOnVmsCreatedListener (
    		EventListener<? extends EventInfo> listener) { return this; }
    @Override public Function<Vm, Double> getVmDestructionDelayFunction () { return vm -> 0.0; }
    @Override public GpuDatacenterBroker setVmDestructionDelayFunction (
    		Function<Vm, Double> function) { return this; }
    @Override public GpuDatacenterBroker setVmDestructionDelay (double delay) { return this; }
    @Override public List<Cloudlet> getCloudletSubmittedList () { return Collections.emptyList(); }
    @Override public <T extends Vm> List<T> getVmFailedList () { return Collections.emptyList(); }
    @Override public boolean isRetryFailedVms () { return false; }
    @Override public double getFailedVmsRetryDelay () { return 0; }
    @Override public void setFailedVmsRetryDelay (double failedVmsRetryDelay) {/**/}
    @Override public boolean isShutdownWhenIdle () { return false; }
    @Override public GpuDatacenterBroker setShutdownWhenIdle (boolean shutdownWhenIdle) { 
    	return this; 
    }
    @Override public GpuDatacenterBroker setVmComparator (Comparator<Vm> comparator) { return this; }
    @Override public void setCloudletComparator (Comparator<Cloudlet> comparator) {/**/}
    @Override public GpuDatacenterBroker submitCloudlet (Cloudlet cloudlet) { return this; }
    @Override public GpuDatacenterBroker submitCloudletList (
    		List<? extends Cloudlet> list) { return this; }
    @Override public GpuDatacenterBroker submitCloudletList (
    		List<? extends Cloudlet> list, double submissionDelay) { return this; }
    @Override public GpuDatacenterBroker submitCloudletList (
    		List<? extends Cloudlet> list, Vm vm) { return this; }
    @Override public GpuDatacenterBroker submitCloudletList (
    		List<? extends Cloudlet> list, Vm vm, double submissionDelay) { return this; }
    @Override public GpuDatacenterBroker submitVm (Vm vm) { return this; }
    @Override public GpuDatacenterBroker submitVmList (List<? extends Vm> list) { return this; }
    @Override public GpuDatacenterBroker submitVmList (
    		List<? extends Vm> list, double submissionDelay) { return this; }
    @Override public double getStartTime() { return -1; }
}