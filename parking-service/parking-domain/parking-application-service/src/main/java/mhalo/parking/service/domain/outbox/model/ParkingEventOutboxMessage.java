package mhalo.parking.service.domain.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.domain.model.event.ParkingEventType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ParkingEventOutboxMessage {
    private UUID id;
    private UUID parkingId;
    private UUID customerId;
    private Instant createdAt;
    private Instant processedAt;
    private String payload;
    private ParkingEventType parkingEventType;
    private OutboxStatus outboxStatus;
    private int version;

    public void setOutboxStatus(OutboxStatus outboxStatus) {
        this.outboxStatus = outboxStatus;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
