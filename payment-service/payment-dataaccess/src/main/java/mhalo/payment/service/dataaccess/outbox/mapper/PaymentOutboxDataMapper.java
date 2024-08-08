package mhalo.payment.service.dataaccess.outbox.mapper;


import mhalo.payment.service.dataaccess.outbox.entity.PaymentEventOutboxMessageEntity;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxDataMapper {
    public PaymentEventOutboxMessage mapPaymentEventOutboxMessageToPaymentEventOutboxMessage(
            PaymentEventOutboxMessageEntity paymentEventOutboxMessageEntity) {
        return PaymentEventOutboxMessage.builder()
                .id(paymentEventOutboxMessageEntity.getId())
                .paymentId(paymentEventOutboxMessageEntity.getPaymentId())
                .customerId(paymentEventOutboxMessageEntity.getCustomerId())
                .parkingId(paymentEventOutboxMessageEntity.getParkingId())
                .createdAt(paymentEventOutboxMessageEntity.getCreatedAt())
                .processedAt(paymentEventOutboxMessageEntity.getProcessedAt())
                .paymentEventType(paymentEventOutboxMessageEntity.getPaymentEventType())
                .outboxStatus(paymentEventOutboxMessageEntity.getOutboxStatus())
                .version(paymentEventOutboxMessageEntity.getVersion())
                .payload(paymentEventOutboxMessageEntity.getPayload())
                .build();
    }

    public PaymentEventOutboxMessageEntity mapPaymentEventOutboxMessageToPaymentEventOutboxMessageEntity(
            PaymentEventOutboxMessage paymentEventOutboxMessage) {
        return PaymentEventOutboxMessageEntity.builder()
                .id(paymentEventOutboxMessage.getId())
                .paymentId(paymentEventOutboxMessage.getPaymentId())
                .customerId(paymentEventOutboxMessage.getCustomerId())
                .parkingId(paymentEventOutboxMessage.getParkingId())
                .createdAt(paymentEventOutboxMessage.getCreatedAt())
                .processedAt(paymentEventOutboxMessage.getProcessedAt())
                .paymentEventType(paymentEventOutboxMessage.getPaymentEventType())
                .outboxStatus(paymentEventOutboxMessage.getOutboxStatus())
                .version(paymentEventOutboxMessage.getVersion())
                .payload(paymentEventOutboxMessage.getPayload())
                .build();
    }
}
