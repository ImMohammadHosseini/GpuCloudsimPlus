package org.cloudbus.cloudsim.gp.core;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Objects;
import java.util.function.Function;

public class GResourceStats<T extends AbstractGpu> {
    private final Function<T, Double> resourceUtilizationFunction;
    private final T machine;
    private final SummaryStatistics stats;
    private double previousTime;
    private double previousUtilization;
    
    protected GResourceStats (final T machine, 
    		final Function<T, Double> resourceUtilizationFunction) {
        this.resourceUtilizationFunction = Objects.requireNonNull(resourceUtilizationFunction);
        this.machine = Objects.requireNonNull(machine);
        this.stats = new SummaryStatistics();
    }
    
    public boolean add(final double time) {
        try {
            if (isNotTimeToAddHistory(time)) {
                return false;
            }

            final double utilization = resourceUtilizationFunction.apply(machine);
            /*If (i) the previous utilization is not zero and the current utilization is zero
            * and (ii) those values don't change, it means the machine has finished
            * and this utilization must not be collected.
            * If that happens, it may reduce accuracy of the utilization mean.
            * For instance, if a machine uses 100% of a resource all the time,
            * when it finishes, the utilization will be zero.
            * If that utilization is collected, the mean won't be 100% anymore.*/
            if((previousUtilization != 0 && utilization == 0) || (machine.isIdle() && previousUtilization > 0)) {
                this.previousUtilization = utilization;
                return false;
            }

            this.stats.addValue(utilization);
            this.previousUtilization = utilization;
            return true;
        } finally {
            this.previousTime = machine.isIdle() ? time : (int)time;
        }
    }
    
    public double getMin () {
        return stats.getMin();
    }

    public double getMax () {
        return stats.getMax();
    }

    public double getMean () {
        return stats.getMean();
    }

    public double getStandardDeviation () {
        return stats.getStandardDeviation();
    }

    public double getVariance () {
        return stats.getVariance();
    }

    public double count () {
        return stats.getN();
    }

    public boolean isEmpty () { return count() == 0; }
    
    protected final boolean isNotTimeToAddHistory (final double time) {
        return time <= 0 ||
               isElapsedTimeSmall(time) ||
               isNotEntireSecondElapsed(time);
    }

    protected final boolean isElapsedTimeSmall (final double time) {
        return time - previousTime < 1 && !machine.isIdle();
    }

    protected final boolean isNotEntireSecondElapsed (final double time) {
        return Math.floor(time) == previousTime && !machine.isIdle();
    }

    protected T getMachine(){
        return machine;
    }

    protected double getPreviousTime() {
        return previousTime;
    }
}
