package mhalo.payment.service.httpclient.apple;

import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommand;
import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommandResponse;
import mhalo.payment.service.domain.ports.output.httpclient.ApplePayClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApplePayClientImpl implements ApplePayClient {

    @Override
    public ApplePayCommandResponse executeTransaction(ApplePayCommand applePayCommand) {
        return ApplePayCommandResponse.builder()
                .providerTransactionId(UUID.randomUUID())
                .isPaymentSuccessful(true)
                .build();
    }
}
