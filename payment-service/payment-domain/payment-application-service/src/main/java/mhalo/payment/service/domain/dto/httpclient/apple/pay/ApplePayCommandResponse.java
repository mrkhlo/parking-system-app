package mhalo.payment.service.domain.dto.httpclient.apple.pay;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ApplePayCommandResponse {
    private final boolean isPaymentSuccessful;
    private final UUID providerTransactionId;
}
