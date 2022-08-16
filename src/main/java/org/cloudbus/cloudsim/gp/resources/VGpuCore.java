package org.cloudbus.cloudsim.gp.resources;

import org.cloudbus.cloudsim.resources.ResourceManageableAbstract;

public final class VGpuCore extends ResourceManageableAbstract {
	public static final VGpuCore NULL = new VGpuCore(0);
	
	private CustomVGpu vgpu;
	private double mips;
	
	public VGpuCore (final CustomVGpu vgpu, final double mips, final long numberOfPes) {
		this(numberOfPes);
        this.vgpu = vgpu;
        setMips(mips);
	}
	
	private VGpuCore (final long numberOfPes){
		super(numberOfPes, "Unit");
    }
	
	public void setMips(final double newMips) {
        if(newMips < 0) {
            throw new IllegalArgumentException("MIPS cannot be negative");
        }

        this.mips = newMips;
    }
	
	public double getMips() {
        return mips;
    }

	public double getTotalMips(){
        return getMips()* getCapacity();
    }
	
	//getNumberOfPes
	@Override
    public long getCapacity() {
        return super.getCapacity();
    }
	
	@Override
    public long getAvailableResource() {
        return super.getAvailableResource();
    }

    @Override
    public long getAllocatedResource() {
        return super.getAllocatedResource();
    }
    
    @Override
    public boolean setCapacity(long numberOfPes) {
        if(numberOfPes <= 0){
            throw new IllegalArgumentException("The Processor's number of PEs must be greater than 0.");
        }
        return super.setCapacity(numberOfPes);
    }

    public CustomVGpu getVgpu() {
        return vgpu;
    }
}
    
