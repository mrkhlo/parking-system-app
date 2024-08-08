package mhalo.parking.service.domain.ports.input.event.listener.payment;


import mhalo.domain.ports.input.event.listener.EventListener;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundEvent;

public interface PaymentRefundEventListener extends EventListener<PaymentRefundEvent> {
}
