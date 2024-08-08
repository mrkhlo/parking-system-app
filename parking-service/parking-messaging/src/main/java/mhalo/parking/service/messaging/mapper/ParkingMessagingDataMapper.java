package mhalo.parking.service.messaging.mapper;

import mhalo.parking.service.core.domain.*;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitStatus;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundStatus;
import mhalo.parking.service.domain.outbox.model.ParkingApprovedEventOutboxPayload;
import mhalo.parking.service.domain.outbox.model.ParkingCreatedEventOutboxPayload;
import mhalo.parking.service.domain.outbox.model.ParkingStoppedEventOutboxPayload;
import org.springframework.stereotype.Component;


@Component
public class ParkingMessagingDataMapper {

    public ParkingCreatedEventAvroModel mapParkingCreatedEventToParkingCreatedEventAvroModel(ParkingCreatedEventOutboxPayload payload) {
        return ParkingCreatedEventAvroModel.newBuilder()
                .setParking(ParkingAvroModel.newBuilder()
                        .setParkingId(payload.getParkingId())
                        .setZoneId(payload.getParkingId())
                        .setCustomerId(payload.getCustomerId())
                        .setTrackingId(payload.getTrackingId())
                        .setStartedAt(payload.getStartedAt())
                        .setStoppedAt(payload.getStoppedAt())
                        .setStartingFee(payload.getStartingFee())
                        .setClosingFee(payload.getClosingFee())
                        .setLicensePlateNumber(payload.getLicensePlateNumber())
                        .build())
                .build();
    }

    public ParkingStoppedEventAvroModel mapParkingStoppedEventToParkingStoppedEventAvroModel(ParkingStoppedEventOutboxPayload payload) {
        return ParkingStoppedEventAvroModel.newBuilder()
                .setParking(ParkingAvroModel.newBuilder()
                        .setParkingId(payload.getParkingId())
                        .setZoneId(payload.getParkingId())
                        .setCustomerId(payload.getCustomerId())
                        .setStartingFee(payload.getStartingFee())
                        .setClosingFee(payload.getClosingFee())
                        .setTrackingId(payload.getTrackingId())
                        .setStoppedAt(payload.getStoppedAt())
                        .setStartedAt(payload.getStartedAt())
                        .setLicensePlateNumber(payload.getLicensePlateNumber())
                        .build())
                .build();
    }

    public ParkingApprovedEventAvroModel mapParkingApprovedEventOutboxPayloadToParkingApprovedEventAvroModel(ParkingApprovedEventOutboxPayload payload) {
        return ParkingApprovedEventAvroModel.newBuilder()
                .setParkingId(payload.getParkingId())
                .setCustomerId(payload.getCustomerId())
                .build();
    }

    public PaymentDebitEvent mapPaymentDebitEventAvroModelToPaymentDebitEvent(PaymentDebitEventAvroModel paymentDebitEventAvroModel) {
        PaymentDebitStatus paymentDebitStatus = PaymentDebitStatus.valueOf(paymentDebitEventAvroModel.getPayment().getPaymentStatus().name());
        return PaymentDebitEvent.builder()
                .parkingId(paymentDebitEventAvroModel.getPayment().getParkingId())
                .paymentId(paymentDebitEventAvroModel.getPayment().getPaymentId())
                .paymentStatus(paymentDebitStatus)
                .build();
    }

    public PaymentRefundEvent mapPaymentRefundEventAvroModelToPaymentRefundEvent(PaymentRefundEventAvroModel paymentRefundEventAvroModel) {
        PaymentRefundStatus paymentRefundStatus = PaymentRefundStatus.valueOf(paymentRefundEventAvroModel.getPayment().getPaymentStatus().name());
        return PaymentRefundEvent.builder()
                .parkingId(paymentRefundEventAvroModel.getPayment().getParkingId())
                .paymentId(paymentRefundEventAvroModel.getPayment().getPaymentId())
                .paymentStatus(paymentRefundStatus)
                .build();
    }
}
