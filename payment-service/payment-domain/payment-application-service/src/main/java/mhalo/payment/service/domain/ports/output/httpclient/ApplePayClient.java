package mhalo.payment.service.domain.ports.output.httpclient;

import jakarta.validation.Valid;
import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommand;
import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommandResponse;

public interface ApplePayClient {
    ApplePayCommandResponse executeTransaction(@Valid ApplePayCommand applePayCommand);
}
