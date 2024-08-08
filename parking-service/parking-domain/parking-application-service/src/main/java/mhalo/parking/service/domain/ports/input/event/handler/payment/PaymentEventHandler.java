package mhalo.parking.service.domain.ports.input.event.handler.payment;

import jakarta.validation.Valid;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundEvent;

public interface PaymentEventHandler {
    void processPaymentDebitEvent(@Valid PaymentDebitEvent paymentDebitEvent);
    void processPaymentDebitFailedEvent(@Valid PaymentDebitEvent paymentDebitEvent);
    void processPaymentRefundEvent(@Valid PaymentRefundEvent event);
}
