package mhalo.parking.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitStatus;
import mhalo.parking.service.domain.ports.input.event.listener.payment.PaymentDebitEventListener;
import mhalo.parking.service.domain.ports.input.service.ParkingApplicationService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDebitEventListenerImpl implements PaymentDebitEventListener {

    private final ParkingApplicationService parkingApplicationService;

    @Override
    public void process(PaymentDebitEvent event) {
        if (event.getPaymentStatus() == PaymentDebitStatus.DEBITED) {
            parkingApplicationService.approveParkingCreation(event);
        } else {
            parkingApplicationService.declineParkingCreation(event);
        }
    }
}
