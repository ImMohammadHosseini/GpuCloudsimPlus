package org.cloudbus.cloudsim.gp.resources;

import static java.util.Objects.requireNonNull;
import java.util.Collections;
import java.util.List;

import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.resources.ResourceManageableAbstract;
import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisioner;
import org.cloudbus.cloudsim.gp.provisioners.GpuResourceProvisionerSimple;



public class GpuSimple implements Gpu {
	
	private long id;
	private String type;
	private final Ram ram;
    private final Bandwidth bw;
	private List<Pe> gpuPeList;
	private GpuResourceProvisioner gpuGddramProvisioner;
	private GpuResourceProvisioner gpuBwProvisioner;
	
	public GpuSimple (long id, String type, final long ram, final long bw,
			final List<Pe> peList) {
		this.setId(id);
		this.setType(type);
		
		this.ram = new Ram(ram);
		this.bw = new Bandwidth(bw);
		
		this.setGpuGddramProvisioner(new GpuResourceProvisionerSimple());
        this.setGpuBwProvisioner(new GpuResourceProvisionerSimple());
        this.setGpuPeList(peList);
	}
	
	@Override 
	public final void setId (long id) {
		this.id = id;
	}
	@Override 
	public long getId () {
        return id;
    }
	
	public void setType (String type) {
		this.type = type;
	}
	
	public String getType () {
		return type;
	}
	
	public void setGpuPeList (final List<Pe> peList) {
		if(requireNonNull(peList).isEmpty()){
            throw new IllegalArgumentException("The PE list for a Gpu cannot be empty");
        }
		
		this.gpuPeList = peList;
		
		//need to be complete
	}

	@Override 
	public List<Pe> getGpuPeList () {
		return gpuPeList;
    }
	
	
	@Override 
	public Gpu setGpuGddramProvisioner (GpuResourceProvisioner gpuGddramProvisioner) {
		this.gpuGddramProvisioner = requireNonNull(gpuGddramProvisioner);
        this.gpuGddramProvisioner.setResources(ram, vgpu -> ((CustomVGpu)vgpu).getRam());
        return this;
	}
	
	@Override 
	public GpuResourceProvisioner getGpuGddramProvisioner () {
        return gpuGddramProvisioner;
    }
        
    @Override 
    public Gpu setGpuBwProvisioner (GpuResourceProvisioner gpuBwProvisioner) {
    	this.gpuBwProvisioner = requireNonNull(gpuBwProvisioner);
        this.gpuBwProvisioner.setResources(bw, vgpu -> ((CustomVGpu)vgpu).getBw());
    	return this;
    }
    
    @Override 
    public GpuResourceProvisioner getGpuBwProvisioner () {
        return gpuBwProvisioner;
    }
}