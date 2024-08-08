package mhalo.payment.service.domain.exception;

import mhalo.domain.model.event.exception.DomainException;

public class PaymentDomainException extends DomainException {
    public PaymentDomainException() {
    }

    public PaymentDomainException(String message) {
        super(message);
    }

    public PaymentDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
