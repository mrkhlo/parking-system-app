package mhalo.parking.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import mhalo.domain.model.event.model.ResultWithDomainEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitEvent;
import mhalo.parking.service.domain.dto.rest.start.StartParkingCommand;
import mhalo.parking.service.domain.dto.rest.start.StartParkingResponse;
import mhalo.parking.service.domain.dto.rest.stop.StopParkingCommand;
import mhalo.parking.service.domain.dto.rest.track.TrackParkingResponse;
import mhalo.parking.service.domain.exception.ParkingDomainException;
import mhalo.parking.service.domain.exception.ParkingNotFoundException;
import mhalo.parking.service.domain.mapper.ParkingDataMapper;
import mhalo.parking.service.domain.model.Parking;
import mhalo.parking.service.domain.model.ParkingStatus;
import mhalo.parking.service.domain.model.Zone;
import mhalo.parking.service.domain.model.event.ParkingApprovedEvent;
import mhalo.parking.service.domain.model.event.ParkingCreatedEvent;
import mhalo.parking.service.domain.model.event.ParkingStoppedEvent;
import mhalo.parking.service.domain.ports.input.service.ParkingApplicationService;
import mhalo.parking.service.domain.ports.output.httpclient.CustomerRestClient;
import mhalo.parking.service.domain.ports.output.httpclient.ZoneRestClient;
import mhalo.parking.service.domain.ports.output.repository.ParkingRepository;
import mhalo.parking.service.domain.outbox.scheduler.ParkingEventOutboxHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class ParkingApplicationServiceImpl implements ParkingApplicationService {

    private final CustomerRestClient customerRestClient;
    private final ZoneRestClient zoneRestClient;
    private final ParkingFeeCalculator parkingFeeCalculator;
    private final ParkingDataMapper parkingDataMapper;
    private final ParkingRepository parkingRepository;
    private final ParkingDomainService parkingDomainService;
    private final ParkingEventOutboxHelper parkingEventOutboxHelper;
    private final Clock clock;

    @Override
    @Transactional
    public StartParkingResponse startParking(StartParkingCommand startParkingCommand) {
        validateCustomerExists(startParkingCommand.getCustomerId());
        Instant startingAt = Instant.now(clock);
        Zone parkingZone = zoneRestClient.getZoneById(startParkingCommand.getZoneId());

        parkingDomainService.validateParkingZoneInterval(parkingZone, startingAt);

        ResultWithDomainEvent<Parking, ParkingCreatedEvent> resultWithDomainEvent = parkingDomainService
                .createParking(startParkingCommand.getCustomerId(), startParkingCommand.getLicensePlateNumber(), startingAt, parkingZone);

        Parking parking = parkingRepository.save(resultWithDomainEvent.getResult());
        parkingEventOutboxHelper.persistOutboxMessageFromEvent(resultWithDomainEvent.getEvent());

        log.info("Parking with id: {} created and is in {} status", parking.getParkingStatus(), parking.getId());
        return new StartParkingResponse(parking.getTrackingId(), parking.getId());
    }

    @Override
    @Transactional
    public void approveParkingCreation(PaymentDebitEvent paymentDebitEvent) {
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
    public void declineParkingCreation(PaymentDebitEvent paymentDebitEvent) {
        Parking parking = findParkingById(paymentDebitEvent.getParkingId());
        ParkingStatus parkingStatus = parking.getParkingStatus();

        if (ParkingStatus.CANCELLED == parkingStatus) {
            log.info("Parking with id: {} is already cancelled.", paymentDebitEvent.getParkingId());
            return;
        }

        parkingDomainService.declineParking(parking);
        parkingRepository.save(parking);
    }

    @Override
    @Transactional
    public void stopParking(StopParkingCommand stopParkingCommand) {
        Parking parking = findParkingById(stopParkingCommand.getParkingId());

        ParkingStatus parkingStatus = parking.getParkingStatus();
        if (ParkingStatus.STOP_PENDING == parkingStatus) {
            log.info("Parking with id: {} is already stopped", parking.getId());
            return;
        }

        Instant stoppedAt = Instant.now(clock);
        Zone parkingZone = zoneRestClient.getZoneById(parking.getZoneId());
        ResultWithDomainEvent<Parking, ParkingStoppedEvent> resultWithDomainEvent = parkingDomainService
                .stopParking(parking, stoppedAt, parkingZone);

        parkingRepository.save(resultWithDomainEvent.getResult());
        parkingEventOutboxHelper.persistOutboxMessageFromEvent(resultWithDomainEvent.getEvent());
        log.info("Parking with id: {} is stopping", parking.getId());
    }

    @Override
    @Transactional
    public void confirmParkingStopping(PaymentRefundEvent event) {
        Parking parking = findParkingById(event.getParkingId());
        ParkingStatus parkingStatus = parking.getParkingStatus();

        if (ParkingStatus.STOPPED == parkingStatus) {
            log.info("Parking with id: {} is already confirmed stopped", event.getParkingId());
            return;
        }

        parkingDomainService.approveStopParking(parking);
        parkingRepository.save(parking);
        log.info("Parking with id: {} is stopped", event.getParkingId());
    }

    @Override
    @Transactional(readOnly = true)
    public TrackParkingResponse trackParking(UUID trackingId) {
        Parking parking = findParkingByTrackingId(trackingId);
        return parkingDataMapper.mapParkingToTrackParkingResponse(parking);
    }

    private Parking findParkingById(UUID parkingId) {
        Optional<Parking> parkingOpt = parkingRepository.findById(parkingId);
        if (parkingOpt.isEmpty()) {
            throw new ParkingNotFoundException("Parking not found with id: %s".formatted(parkingId));
        }
        return parkingOpt.get();
    }

    private Parking findParkingByTrackingId(UUID parkingTrackingId) {
        Optional<Parking> parkingOpt = parkingRepository.findByTrackingId(parkingTrackingId);
        if (parkingOpt.isEmpty()) {
            throw new ParkingNotFoundException("Parking not found with trackingId: %s".formatted(parkingTrackingId));
        }
        return parkingOpt.get();
    }

    private void validateCustomerExists(UUID customerId) {
        boolean isCustomerExists = this.customerRestClient.isCustomerExistsById(customerId);
        if (!isCustomerExists) {
            throw new ParkingDomainException("Customer with id: %s does not exist.".formatted(customerId.toString()));
        }
    }
}
