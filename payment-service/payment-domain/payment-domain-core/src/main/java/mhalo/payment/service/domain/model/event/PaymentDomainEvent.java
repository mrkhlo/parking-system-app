package mhalo.payment.service.domain.model.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mhalo.domain.model.event.event.DomainEvent;
import mhalo.payment.service.domain.model.Payment;
import mhalo.payment.service.domain.model.PaymentStatus;
import mhalo.payment.service.domain.model.Transaction;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public abstract class PaymentDomainEvent implements DomainEvent<Payment> {
    private UUID paymentId;
    private UUID parkingId;
    private UUID customerId;
    private Instant createdAt;
    private Transaction transaction;
    private PaymentStatus paymentStatus;

    public abstract PaymentEventType getEventType();
}
