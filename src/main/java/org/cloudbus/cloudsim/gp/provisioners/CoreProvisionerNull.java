package org.cloudbus.cloudsim.gp.provisioners;

import org.cloudbus.cloudsim.gp.resources.GpuCore;
import org.cloudbus.cloudsim.gp.vgpu.VGpu;

final class CoreProvisionerNull extends GpuResourceProvisionerNull implements CoreProvisioner {
    @Override public void setCore (GpuCore core) {/**/}
    @Override public double getUtilization () {
        return 0;
    }
    @Override public boolean allocateResourceForVGpu (VGpu vgpu, double newTotalVGpuResource) {
        return false;
    }
}
