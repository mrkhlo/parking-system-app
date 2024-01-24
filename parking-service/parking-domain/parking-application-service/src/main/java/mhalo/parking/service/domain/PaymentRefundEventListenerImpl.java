package mhalo.parking.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundStatus;
import mhalo.parking.service.domain.ports.input.event.listener.payment.PaymentRefundEventListener;
import mhalo.parking.service.domain.ports.input.service.ParkingApplicationService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundEventListenerImpl implements PaymentRefundEventListener {

    private final ParkingApplicationService parkingApplicationService;

    @Override
    public void process(PaymentRefundEvent event) {
        if (event.getPaymentStatus() == PaymentRefundStatus.REFUNDED) {
            parkingApplicationService.confirmParkingStopping(event);
        } else {
            // not handled currently
            log.info("Refund (startingFee - closingFee) failed for parking with id : {}", event.getParkingId());
        }
    }
}
