package mhalo.payment.service.domain.util;

import lombok.RequiredArgsConstructor;
import mhalo.payment.service.domain.model.event.PaymentEventType;
import mhalo.payment.service.domain.ports.output.event.publisher.payment.PaymentDebitEventPublisher;
import mhalo.payment.service.domain.ports.output.event.publisher.payment.PaymentEventPublisher;
import mhalo.payment.service.domain.ports.output.event.publisher.payment.PaymentRefundEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublishers {

    private final PaymentRefundEventPublisher paymentRefundEventPublisher;
    private final PaymentDebitEventPublisher paymentDebitEventPublisher;

    public PaymentEventPublisher getPaymentEventPublisher(PaymentEventType paymentEventType) {
        return switch (paymentEventType) {
            case DEBIT -> paymentDebitEventPublisher;
            case REFUND -> paymentRefundEventPublisher;
        };
    }
}
