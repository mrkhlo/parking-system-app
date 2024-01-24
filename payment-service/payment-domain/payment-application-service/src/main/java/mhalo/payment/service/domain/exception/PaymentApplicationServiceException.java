package mhalo.payment.service.domain.exception;

import lombok.NoArgsConstructor;
import mhalo.payment.service.domain.exception.PaymentDomainException;

@NoArgsConstructor
public class PaymentApplicationServiceException extends PaymentDomainException {

    public PaymentApplicationServiceException(String message) {
        super(message);
    }

    public PaymentApplicationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
