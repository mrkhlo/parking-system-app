package mhalo.parking.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitStatus;
import mhalo.parking.service.domain.ports.input.event.handler.payment.PaymentEventHandler;
import mhalo.parking.service.domain.ports.input.event.listener.payment.PaymentDebitEventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDebitEventListenerImpl implements PaymentDebitEventListener {

    private final PaymentEventHandler paymentEventHandler;

    @Override
    public void process(PaymentDebitEvent event) {
        if (event.getPaymentStatus() == PaymentDebitStatus.DEBITED) {
            paymentEventHandler.processPaymentDebitEvent(event);
        } else {
            paymentEventHandler.processPaymentDebitFailedEvent(event);
        }
    }
}
