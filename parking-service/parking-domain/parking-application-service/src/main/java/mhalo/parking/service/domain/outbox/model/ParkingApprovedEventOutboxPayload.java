package mhalo.parking.service.domain.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ParkingApprovedEventOutboxPayload {
    private UUID parkingId;
    private UUID customerId;
}
