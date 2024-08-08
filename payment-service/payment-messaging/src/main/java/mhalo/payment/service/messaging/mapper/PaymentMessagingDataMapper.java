package mhalo.payment.service.messaging.mapper;

import mhalo.domain.model.event.model.Money;
import mhalo.parking.service.core.domain.*;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.dto.event.ParkingStoppedEvent;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxPayload;
import mhalo.payment.service.domain.outbox.model.PaymentEventTransactionOutboxPayload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentMessagingDataMapper {

    public ParkingCreatedEvent mapParkingCreatedEventAvroModelToParkingCreatedEvent(
            ParkingCreatedEventAvroModel parkingCreatedEventAvroModel, UUID eventId) {
        return ParkingCreatedEvent.builder()
                .eventId(eventId)
                .parkingId(parkingCreatedEventAvroModel.getParking().getParkingId())
                .customerId(parkingCreatedEventAvroModel.getParking().getParkingId())
                .startingFee(new Money(parkingCreatedEventAvroModel.getParking().getStartingFee()))
                .build();
    }

    public ParkingStoppedEvent mapParkingStoppedEventAvroModelToParkingStoppedEvent(
            ParkingStoppedEventAvroModel parkingStoppedEventAvroModel, UUID eventId) {
        return ParkingStoppedEvent.builder()
                .eventId(eventId)
                .customerId(parkingStoppedEventAvroModel.getParking().getCustomerId())
                .parkingId(parkingStoppedEventAvroModel.getParking().getParkingId())
                .closingFee(new Money(parkingStoppedEventAvroModel.getParking().getClosingFee()))
                .startedAt(parkingStoppedEventAvroModel.getParking().getStartedAt())
                .stoppedAt(parkingStoppedEventAvroModel.getParking().getStoppedAt())
                .build();
    }

    public PaymentRefundEventAvroModel mapPaymentEventOutboxPayloadToPaymentRefundEventAvroModel(
            PaymentEventOutboxPayload outboxMessagePayload) {
        PaymentAvroModel paymentAvroModel = mapPaymentEventOutboxPayloadToPaymentAvroModel(outboxMessagePayload);
        return PaymentRefundEventAvroModel.newBuilder()
                .setPayment(paymentAvroModel)
                .build();
    }

    public PaymentDebitEventAvroModel mapPaymentEventOutboxPayloadToPaymentDebitEventAvroModel(
            PaymentEventOutboxPayload outboxMessagePayload) {
        PaymentAvroModel paymentAvroModel = mapPaymentEventOutboxPayloadToPaymentAvroModel(outboxMessagePayload);
        return PaymentDebitEventAvroModel.newBuilder()
                .setPayment(paymentAvroModel)
                .build();
    }

    private PaymentAvroModel mapPaymentEventOutboxPayloadToPaymentAvroModel(PaymentEventOutboxPayload outboxPayload) {
        TransactionAvroModel transactionAvroModel = mapPaymentEventTransactionOutboxPayloadToTransactionAvroModel(
                outboxPayload.getTransaction());
        return PaymentAvroModel.newBuilder()
                .setPaymentId(outboxPayload.getPaymentId())
                .setCustomerId(outboxPayload.getCustomerId())
                .setParkingId(outboxPayload.getParkingId())
                .setCreatedAt(outboxPayload.getCreatedAt())
                .setTransaction(transactionAvroModel)
                .setPaymentStatus(PaymentStatusAvroModel.valueOf(outboxPayload.getPaymentStatus().name()))
                .build();
    }

    private TransactionAvroModel mapPaymentEventTransactionOutboxPayloadToTransactionAvroModel(
            PaymentEventTransactionOutboxPayload transactionOutboxPayload) {
        return TransactionAvroModel.newBuilder()
                .setTransactionId(transactionOutboxPayload.getTransactionId())
                .setProviderTransactionId(transactionOutboxPayload.getProviderTransactionId())
                .setExecutedAt(transactionOutboxPayload.getExecutedAt())
                .setTransactionType(TransactionTypeAvroModel.valueOf(transactionOutboxPayload.getTransactionType().name()))
                .setTransactionStatus(TransactionStatusAvroModel.valueOf(transactionOutboxPayload.getTransactionStatus().name()))
                .setAmount(transactionOutboxPayload.getAmount())
                .build();
    }
}
