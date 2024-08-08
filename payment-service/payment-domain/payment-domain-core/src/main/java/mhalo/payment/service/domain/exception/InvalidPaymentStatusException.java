package mhalo.payment.service.domain.exception;

import lombok.Getter;
import mhalo.payment.service.domain.model.PaymentStatus;

@Getter
public class InvalidPaymentStatusException extends PaymentDomainException {
    private final PaymentStatus actualStatus;
    private final PaymentStatus expectedStatus;

    public InvalidPaymentStatusException(PaymentStatus expectedStatus, PaymentStatus actualStatus, String message) {
        super(message);
        this.actualStatus = actualStatus;
        this.expectedStatus = expectedStatus;
    }

    public InvalidPaymentStatusException(PaymentStatus actualStatus, PaymentStatus expectedStatus,
                                         String message, Throwable cause) {
        super(message, cause);
        this.actualStatus = actualStatus;
        this.expectedStatus = expectedStatus;
    }
}
