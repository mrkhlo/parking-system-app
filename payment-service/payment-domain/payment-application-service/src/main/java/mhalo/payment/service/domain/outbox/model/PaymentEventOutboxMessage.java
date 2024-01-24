package mhalo.payment.service.domain.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.domain.model.event.PaymentEventType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PaymentEventOutboxMessage {
    private UUID id;
    private UUID paymentId;
    private UUID parkingId;
    private UUID customerId;
    private Instant createdAt;
    private Instant processedAt;
    private String payload;
    private PaymentEventType paymentEventType;
    private OutboxStatus outboxStatus;
    private int version;

    public void setOutboxStatus(OutboxStatus outboxStatus) {
        this.outboxStatus = outboxStatus;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
