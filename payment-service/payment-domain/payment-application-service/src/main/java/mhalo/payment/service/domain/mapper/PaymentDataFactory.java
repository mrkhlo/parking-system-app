package mhalo.payment.service.domain.mapper;


import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.domain.model.Payment;
import mhalo.payment.service.domain.paramwrapper.CreatePaymentDetails;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;

import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommandResponse;

import org.springframework.stereotype.Component;

@Component
public class PaymentDataFactory {
    public CreatePaymentDetails makeCreatePaymentDetails(
            ParkingCreatedEvent parkingCreatedEvent, ApplePayCommandResponse applePayCommandResponse) {
        return CreatePaymentDetails.builder()
                .isAppleTransactionSuccess(applePayCommandResponse.isPaymentSuccessful())
                .providerTransactionId(applePayCommandResponse.getProviderTransactionId())
                .startingFee(parkingCreatedEvent.getStartingFee())
                .customerId(parkingCreatedEvent.getCustomerId())
                .parkingId(parkingCreatedEvent.getParkingId())
                .build();
    }
}
