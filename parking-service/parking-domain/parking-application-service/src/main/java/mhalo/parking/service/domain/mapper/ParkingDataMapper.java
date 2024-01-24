package mhalo.parking.service.domain.mapper;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.parking.service.domain.dto.rest.track.TrackParkingResponse;
import mhalo.parking.service.domain.model.Parking;
import mhalo.parking.service.domain.model.event.ParkingApprovedEvent;
import mhalo.parking.service.domain.model.event.ParkingCreatedEvent;
import mhalo.parking.service.domain.model.event.ParkingStoppedEvent;
import mhalo.parking.service.domain.outbox.model.ParkingApprovedEventOutboxPayload;
import mhalo.parking.service.domain.outbox.model.ParkingCreatedEventOutboxPayload;
import mhalo.parking.service.domain.outbox.model.ParkingStoppedEventOutboxPayload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParkingDataMapper {

    public TrackParkingResponse mapParkingToTrackParkingResponse(Parking parking) {
        return TrackParkingResponse.builder()
                .parkingTrackId(parking.getTrackingId())
                .parkingStatus(parking.getParkingStatus())
                .build();
    }

    public ParkingCreatedEventOutboxPayload mapParkingCreatedEventToParkingCreatedEventOutboxPayload(
            ParkingCreatedEvent parkingCreatedEvent) {
        Parking parking = parkingCreatedEvent.getParking();

        return ParkingCreatedEventOutboxPayload.builder()
                .parkingId(parking.getId())
                .customerId(parking.getCustomerId())
                .zoneId(parking.getZoneId())
                .trackingId(parking.getTrackingId())
                .licensePlateNumber(parking.getLicensePlateNumber())
                .startingFee(parking.getStartingFee().getAmount())
                .startedAt(parking.getStartedAt())
                .stoppedAt(parking.getStoppedAt())
                .parkingStatus(parking.getParkingStatus())
                .build();
    }

    public ParkingStoppedEventOutboxPayload mapParkingStoppedEventToParkingStoppedEventOutboxPayload(
            ParkingStoppedEvent parkingStoppedEvent) {
        Parking parking = parkingStoppedEvent.getParking();

        return ParkingStoppedEventOutboxPayload.builder()
                .parkingId(parking.getId())
                .customerId(parking.getCustomerId())
                .zoneId(parking.getZoneId())
                .trackingId(parking.getTrackingId())
                .licensePlateNumber(parking.getLicensePlateNumber())
                .startingFee(parking.getStartingFee().getAmount())
                .closingFee(parking.getClosingFee().getAmount())
                .startedAt(parking.getStartedAt())
                .stoppedAt(parking.getStoppedAt())
                .parkingStatus(parking.getParkingStatus())
                .build();
    }

    public ParkingApprovedEventOutboxPayload mapParkingApprovedEventToParkingApprovedEventOutboxPayload(
            ParkingApprovedEvent parkingApprovedEvent) {
        return ParkingApprovedEventOutboxPayload.builder()
                .customerId(parkingApprovedEvent.getCustomerId())
                .parkingId(parkingApprovedEvent.getParkingId())
                .build();
    }

}
