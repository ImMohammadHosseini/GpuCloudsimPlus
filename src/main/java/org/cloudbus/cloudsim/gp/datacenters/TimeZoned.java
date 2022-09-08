package org.cloudbus.cloudsim.gp.datacenters;

import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.cloudbus.cloudsim.gp.vms.GpuVm;

import java.util.List;
import java.util.Objects;

import static org.cloudbus.cloudsim.util.TimeUtil.hoursToMinutes;

public interface TimeZoned {
    int MIN_TIME_ZONE_OFFSET = -12;
    int MAX_TIME_ZONE_OFFSET =  14;

    double getTimeZone();

    TimeZoned setTimeZone(double timeZone);

    default double validateTimeZone(final double timeZone) {
        if(timeZone < MIN_TIME_ZONE_OFFSET || timeZone > MAX_TIME_ZONE_OFFSET){
            final var msg = "Timezone offset must be between [%d and %d].";
            throw new IllegalArgumentException(String.format(msg, MIN_TIME_ZONE_OFFSET, MAX_TIME_ZONE_OFFSET));
        }

        return timeZone;
    }

    static GpuDatacenter closestDatacenter(final GpuVm vm, final List<GpuDatacenter> datacenters){
        if(Objects.requireNonNull(datacenters).isEmpty()){
            throw new IllegalArgumentException("The list of Datacenters is empty.");
        }

        if(datacenters.size() == 1){
            return datacenters.get(0);
        }

        final var iterator = vm.getTimeZone() <= 0 ? datacenters.listIterator() : 
        	new ReverseListIterator<>(datacenters);

        var currentDc = GpuDatacenter.NULL;
        var previousDc = currentDc;
        while(iterator.hasNext()) {
            currentDc = iterator.next();
            
            if(vm.distance(currentDc) > vm.distance(previousDc)){
                return previousDc;
            }

            previousDc = currentDc;
        }

        return currentDc;
    }

    default double distance(final TimeZoned other) {
        return Math.abs(other.getTimeZone() - this.getTimeZone());
    }

    static String format(final double timeZone){
        final double decimals = timeZone - (int) timeZone;
        final String formatted = decimals == 0 ?
                                    String.format("GMT%+.0f", timeZone) :
                                    String.format("GMT%+d:%2.0f", (int)timeZone, 
                                    		hoursToMinutes(decimals));
        return String.format("%-8s", formatted);
    }

}
