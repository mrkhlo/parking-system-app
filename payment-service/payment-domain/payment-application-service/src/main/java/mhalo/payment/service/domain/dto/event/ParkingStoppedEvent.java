package mhalo.payment.service.domain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import mhalo.domain.model.event.model.Money;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ParkingStoppedEvent {
    private final UUID eventId;
    private final UUID parkingId;
    private final UUID customerId;
    private final Money closingFee;
    private final Instant startedAt;
    private final Instant stoppedAt;
}
