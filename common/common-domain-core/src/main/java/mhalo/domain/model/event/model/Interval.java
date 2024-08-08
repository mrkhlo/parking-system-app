package mhalo.domain.model.event.model;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public class Interval {
    private final LocalTime startTime;
    private final LocalTime endTime;

    public Interval(LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Interval start time must be before end time");
        }

        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean containsTimeInclusive(LocalTime time) {
        boolean outsideInterval = time.isBefore(startTime) || time.isAfter(endTime);
        return !outsideInterval;
    }
}
