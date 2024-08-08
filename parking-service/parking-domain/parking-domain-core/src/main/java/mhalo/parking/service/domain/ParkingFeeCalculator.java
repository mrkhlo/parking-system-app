package mhalo.parking.service.domain;

import mhalo.domain.model.event.DomainConstants;
import mhalo.domain.model.event.model.Interval;
import mhalo.domain.model.event.model.Money;
import mhalo.domain.model.event.model.Rate;
import mhalo.parking.service.domain.model.Zone;

import java.math.BigDecimal;
import java.time.*;

public class ParkingFeeCalculator {

    public Money calculateStartingFee(Instant parkingStartAt, Zone parkingZone) {
        Rate rate = parkingZone.getRate();
        Interval payInterval = parkingZone.getPayInterval();

        LocalTime parkingStartTime = LocalDateTime
                .ofInstant(parkingStartAt, ZoneId.of(DomainConstants.UTC))
                .toLocalTime();

        boolean isFreeInterval = !payInterval.containsTimeInclusive(parkingStartTime);
        if (isFreeInterval) return Money.ZERO;

        Duration parkingDuration = Duration.between(parkingStartTime, payInterval.getEndTime());
        double multiplier = divide(parkingDuration, rate.getDuration());
        return rate.getAmount().multiply(BigDecimal.valueOf(multiplier));
    }

    public Money calculateClosingFee(Zone parkingZone, Instant parkingStartedAt,
                                     Instant parkingStoppedAt, Money startingFee) {
        boolean stoppedOnSameDay = datesOnSameDay(parkingStartedAt, parkingStoppedAt);
        if (!stoppedOnSameDay) return startingFee;

        Interval payInterval = parkingZone.getPayInterval();
        Rate rate = parkingZone.getRate();
        LocalTime parkingStartTime = LocalDateTime
                .ofInstant(parkingStartedAt, ZoneId.of(DomainConstants.UTC))
                .toLocalTime();
        LocalTime parkingStopTime = LocalDateTime
                .ofInstant(parkingStoppedAt, ZoneId.of(DomainConstants.UTC))
                .toLocalTime();

        boolean isAfterPaidInterval = !payInterval.containsTimeInclusive(parkingStopTime);
        if (isAfterPaidInterval) return startingFee;

        Duration parkingDuration = Duration.between(parkingStartTime, parkingStopTime);
        double multiplier = divide(parkingDuration, rate.getDuration());
        return rate.getAmount().multiply(BigDecimal.valueOf(multiplier));
    }

    private boolean datesOnSameDay(Instant instantA, Instant instantB) {
        LocalDate dateA = instantA.atZone(ZoneId.of(DomainConstants.UTC)).toLocalDate();
        LocalDate dateB = instantB.atZone(ZoneId.of(DomainConstants.UTC)).toLocalDate();
        return dateA.equals(dateB);
    }

    private double divide(Duration dividend, Duration divisor) {
        return (double) dividend.toMillis() / divisor.toMillis();
    }
}
