package mhalo.payment.service.domain.mapper;

import mhalo.payment.service.domain.model.Transaction;
import mhalo.payment.service.domain.model.event.PaymentDomainEvent;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxPayload;
import mhalo.payment.service.domain.outbox.model.PaymentEventTransactionOutboxPayload;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataMapper {

    public PaymentEventTransactionOutboxPayload mapPaymentTransactionToPaymentEventTransactionOutboxPayload(Transaction transaction) {
        return PaymentEventTransactionOutboxPayload.builder()
                .transactionId(transaction.getId())
                .providerTransactionId(transaction.getProviderTransactionId())
                .amount(transaction.getAmount().getAmount())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getTransactionStatus())
                .executedAt(transaction.getExecutedAt())
                .build();
    }

    public PaymentEventOutboxPayload mapPaymentDomainEventToPaymentEventOutboxPayload(
            PaymentDomainEvent paymentDomainEvent) {
        PaymentEventTransactionOutboxPayload outboxTransaction = mapPaymentTransactionToPaymentEventTransactionOutboxPayload(
                paymentDomainEvent.getTransaction());
        return PaymentEventOutboxPayload.builder()
                .paymentId(paymentDomainEvent.getPaymentId())
                .parkingId(paymentDomainEvent.getParkingId())
                .customerId(paymentDomainEvent.getCustomerId())
                .createdAt(paymentDomainEvent.getCreatedAt())
                .paymentStatus(paymentDomainEvent.getPaymentStatus())
                .transaction(outboxTransaction)
                .build();
    }


}
