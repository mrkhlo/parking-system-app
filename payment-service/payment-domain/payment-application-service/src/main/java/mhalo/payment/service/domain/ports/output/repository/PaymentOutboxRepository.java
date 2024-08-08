package mhalo.payment.service.domain.ports.output.repository;

import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;

import java.util.List;

public interface PaymentOutboxRepository {
    PaymentEventOutboxMessage save(PaymentEventOutboxMessage paymentEventOutboxMessage);
    List<PaymentEventOutboxMessage> getPaymentEventOutboxMessagesByOutboxStatus(OutboxStatus outboxStatus);
}
