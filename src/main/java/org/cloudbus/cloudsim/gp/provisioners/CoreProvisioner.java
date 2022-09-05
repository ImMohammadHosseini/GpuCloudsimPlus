package org.cloudbus.cloudsim.gp.provisioners;

import org.cloudbus.cloudsim.gp.resources.GpuCore;
import org.cloudbus.cloudsim.gp.resources.CustomVGpu;

public interface CoreProvisioner extends GpuResourceProvisioner {

    CoreProvisioner NULL = new CoreProvisionerNull ();

    void setCore (GpuCore core);

    @Override
    boolean allocateResourceForVGpu (CustomVGpu vgpu, long mipsCapacity);

    @Override
    long getAllocatedResourceForVGpu (CustomVGpu vgpu);

    @Override
    long deallocateResourceForVGpu (CustomVGpu vgpu);

    @Override
    long getTotalAllocatedResource ();

    double getUtilization ();
}
