package mhalo.parking.service.domain.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mhalo.parking.service.domain.model.ParkingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ParkingCreatedEventOutboxPayload {
    private UUID parkingId;
    private UUID customerId;
    private UUID zoneId;
    private UUID trackingId;
    private String licensePlateNumber;
    private BigDecimal startingFee;
    private BigDecimal closingFee;
    private Instant startedAt;
    private Instant stoppedAt;
    private ParkingStatus parkingStatus;
}
