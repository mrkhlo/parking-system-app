package mhalo.parking.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.domain.model.event.model.ResultWithDomainEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundEvent;
import mhalo.parking.service.domain.exception.ParkingNotFoundException;
import mhalo.parking.service.domain.model.Parking;
import mhalo.parking.service.domain.model.ParkingStatus;
import mhalo.parking.service.domain.model.event.ParkingApprovedEvent;
import mhalo.parking.service.domain.outbox.scheduler.ParkingEventOutboxHelper;
import mhalo.parking.service.domain.ports.input.event.handler.payment.PaymentEventHandler;
import mhalo.parking.service.domain.ports.output.repository.ParkingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class PaymentEventHandlerImpl implements PaymentEventHandler {

    private final ParkingRepository parkingRepository;
    private final ParkingDomainService parkingDomainService;
    private final ParkingEventOutboxHelper parkingEventOutboxHelper;

    @Override
    @Transactional
    public void processPaymentDebitEvent(PaymentDebitEvent paymentDebitEvent) {
        Parking parking = findParkingById(paymentDebitEvent.getParkingId());
        ParkingStatus parkingStatus = parking.getParkingStatus();

        if (ParkingStatus.CREATED == parkingStatus) {
            log.info("Parking with id: {} is already approved.", paymentDebitEvent.getParkingId());
            return;
        }

        ResultWithDomainEvent<Parking, ParkingApprovedEvent> resultWithDomainEvent = parkingDomainService.approveParking(parking);
        parkingRepository.save(resultWithDomainEvent.getResult());
        parkingEventOutboxHelper.persistOutboxMessageFromEvent(resultWithDomainEvent.getEvent());
        log.info("Parking with id: {} is approved", paymentDebitEvent.getParkingId());
    }

    @Override
    @Transactional
    public void processPaymentDebitFailedEvent(PaymentDebitEvent paymentDebitEvent) {
        Parking parking = findParkingById(paymentDebitEvent.getParkingId());
        ParkingStatus parkingStatus = parking.getParkingStatus();

        if (ParkingStatus.CANCELLED == parkingStatus) {
            log.info("Parking with id: {} is already cancelled.", paymentDebitEvent.getParkingId());
            return;
        }

        parkingDomainService.declineParking(parking);
        parkingRepository.save(parking);
        log.info("Parking with id: {} is cancelled", paymentDebitEvent.getParkingId());
    }

    @Override
    @Transactional
    public void processPaymentRefundEvent(PaymentRefundEvent event) {
        Parking parking = findParkingById(event.getParkingId());
        ParkingStatus parkingStatus = parking.getParkingStatus();

        if (ParkingStatus.STOPPED == parkingStatus) {
            log.info("Parking with id: {} is already confirmed stopped", event.getParkingId());
            return;
        }

        parkingDomainService.approveStopParking(parking);
        parkingRepository.save(parking);
        log.info("Parking with id: {} is confirmed stopped", event.getParkingId());
    }

    private Parking findParkingById(UUID parkingId) {
        Optional<Parking> parkingOpt = parkingRepository.findById(parkingId);
        if (parkingOpt.isEmpty()) {
            throw new ParkingNotFoundException("Parking not found with id: %s".formatted(parkingId));
        }
        return parkingOpt.get();
    }
}
