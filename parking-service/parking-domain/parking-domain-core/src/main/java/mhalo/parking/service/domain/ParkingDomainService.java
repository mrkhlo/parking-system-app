package mhalo.parking.service.domain;

import mhalo.domain.model.event.DomainConstants;
import mhalo.domain.model.event.model.Money;
import mhalo.domain.model.event.model.ResultWithDomainEvent;
import mhalo.parking.service.domain.exception.ParkingStartedInFreeZone;
import mhalo.parking.service.domain.model.Parking;
import mhalo.parking.service.domain.model.ParkingStatus;
import mhalo.parking.service.domain.model.Zone;
import mhalo.parking.service.domain.model.event.ParkingApprovedEvent;
import mhalo.parking.service.domain.model.event.ParkingCreatedEvent;
import mhalo.parking.service.domain.model.event.ParkingStoppedEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

public class ParkingDomainService {

    private ParkingFeeCalculator parkingFeeCalculator;

    public ParkingDomainService(ParkingFeeCalculator parkingFeeCalculator) {
        this.parkingFeeCalculator = parkingFeeCalculator;
    }

    public ParkingDomainService() {}

    public ResultWithDomainEvent<Parking, ParkingCreatedEvent> createParking(UUID customerId,
                                                                             String licensePlateNumber,
                                                                             Instant startedAt,
                                                                             Zone parkingZone) {
        validateParkingZoneInterval(parkingZone, startedAt);
        Money startingFee = parkingFeeCalculator.calculateStartingFee(startedAt, parkingZone);
        Parking parking = Parking.builder()
                .id(UUID.randomUUID())
                .trackingId(UUID.randomUUID())
                .customerId(customerId)
                .zoneId(parkingZone.getId())
                .licensePlateNumber(licensePlateNumber)
                .startingFee(startingFee)
                .parkingStatus(ParkingStatus.CREATE_PENDING)
                .startedAt(startedAt)
                .build();

        ParkingCreatedEvent parkingCreatedEvent = new ParkingCreatedEvent(parking);

        return ResultWithDomainEvent.<Parking, ParkingCreatedEvent>builder()
                .result(parking)
                .event(parkingCreatedEvent)
                .build();
    }

    public ResultWithDomainEvent<Parking, ParkingApprovedEvent> approveParking(Parking parking) {
        parking.approveParkingCreation();
        ParkingApprovedEvent parkingApprovedEvent = new ParkingApprovedEvent(parking.getId(), parking.getCustomerId());
        return ResultWithDomainEvent.<Parking, ParkingApprovedEvent>builder()
                .event(parkingApprovedEvent)
                .result(parking)
                .build();
    }

    public ResultWithDomainEvent<Parking, ParkingStoppedEvent> stopParking(Parking parking, Instant stoppedAt, Zone parkingZone) {
        Money closingFee = parkingFeeCalculator.calculateClosingFee(parkingZone, parking.getStartedAt(),
                stoppedAt, parking.getStartingFee());
        parking.stopParking(stoppedAt, closingFee);
        ParkingStoppedEvent parkingStoppedEvent = new ParkingStoppedEvent(parking);

        return ResultWithDomainEvent.<Parking, ParkingStoppedEvent>builder()
                .event(parkingStoppedEvent)
                .result(parking)
                .build();
    }

    private void validateParkingZoneInterval(Zone parkingZone, Instant parkingStartedAt) {
        LocalTime parkingStartTime = LocalDateTime
                .ofInstant(parkingStartedAt, ZoneId.of(DomainConstants.UTC))
                .toLocalTime();
        boolean isFreeZone = !parkingZone.getPayInterval().containsTimeInclusive(parkingStartTime);
        if (isFreeZone) {
            throw new ParkingStartedInFreeZone("Parking is currently free in zone with id: %s".formatted(parkingZone.getId()));
        }
    }

    public void declineParking(Parking parking) {
        parking.declineParkingCreation();
    }

    public void approveStopParking(Parking parking) {
        parking.approveStopParking();
    }
}
