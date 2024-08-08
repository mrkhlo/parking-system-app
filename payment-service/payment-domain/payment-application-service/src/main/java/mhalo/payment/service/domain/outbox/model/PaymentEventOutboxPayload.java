package mhalo.payment.service.domain.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import mhalo.payment.service.domain.model.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class PaymentEventOutboxPayload {
    private UUID paymentId;
    private UUID parkingId;
    private UUID customerId;
    private Instant createdAt;
    private PaymentEventTransactionOutboxPayload transaction;
    private PaymentStatus paymentStatus;
}
