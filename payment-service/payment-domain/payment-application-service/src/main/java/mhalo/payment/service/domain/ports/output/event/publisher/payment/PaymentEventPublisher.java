package mhalo.payment.service.domain.ports.output.event.publisher.payment;

import mhalo.domain.ports.output.event.publisher.EventPublisher;
import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;

public interface PaymentEventPublisher extends EventPublisher<PaymentEventOutboxMessage, OutboxStatus> {
}
