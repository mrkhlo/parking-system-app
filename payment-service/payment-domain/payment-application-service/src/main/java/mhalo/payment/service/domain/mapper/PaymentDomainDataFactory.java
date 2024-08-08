package mhalo.payment.service.domain.mapper;


import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommandResponse;
import mhalo.payment.service.domain.model.Payment;
import mhalo.payment.service.domain.dto.CreatePaymentDetails;
import mhalo.payment.service.domain.dto.RefundPaymentDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PaymentDomainDataFactory {
    public CreatePaymentDetails makeCreatePaymentDetails(
            ParkingCreatedEvent parkingCreatedEvent, ApplePayCommandResponse applePayCommandResponse,
            Instant createdAt, Instant transactionExecutedAt) {
        return CreatePaymentDetails.builder()
                .isAppleTransactionSuccess(applePayCommandResponse.isPaymentSuccessful())
                .providerTransactionId(applePayCommandResponse.getProviderTransactionId())
                .startingFee(parkingCreatedEvent.getStartingFee())
                .customerId(parkingCreatedEvent.getCustomerId())
                .parkingId(parkingCreatedEvent.getParkingId())
                .createdAt(createdAt)
                .executedAt(transactionExecutedAt)
                .build();
    }

    public RefundPaymentDetails makeRefundPaymentDetails(ApplePayCommandResponse response, Payment payment,
                                                          Money refundAmount, Instant transactionExecutedAt) {
        return RefundPaymentDetails.builder()
                .refundAmount(refundAmount)
                .payment(payment)
                .isAppleTransactionSuccess(response.isPaymentSuccessful())
                .providerTransactionId(response.getProviderTransactionId())
                .transactionExecutedAt(transactionExecutedAt)
                .build();
    }
}
