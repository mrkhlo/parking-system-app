package mhalo.parking.service.domain;

import mhalo.domain.model.event.model.Money;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitStatus;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundEvent;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundStatus;
import mhalo.parking.service.domain.exception.InvalidParkingStatusException;
import mhalo.parking.service.domain.exception.ParkingNotFoundException;
import mhalo.parking.service.domain.mapper.ParkingDataMapper;
import mhalo.parking.service.domain.model.Parking;
import mhalo.parking.service.domain.model.ParkingStatus;
import mhalo.parking.service.domain.model.event.ParkingApprovedEvent;
import mhalo.parking.service.domain.outbox.scheduler.ParkingEventOutboxHelper;
import mhalo.parking.service.domain.ports.output.httpclient.ZoneRestClient;
import mhalo.parking.service.domain.ports.output.repository.ParkingRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestPaymentEventHandlerImpl {

    @Mock
    private ZoneRestClient zoneRestClient;
    @Mock
    private ParkingFeeCalculator parkingFeeCalculator;
    @Spy
    private ParkingDataMapper parkingDataMapper;
    @Mock
    private ParkingRepository parkingRepository;
    @InjectMocks
    private ParkingDomainService parkingDomainService = Mockito.spy(new ParkingDomainService());
    @Mock
    private ParkingEventOutboxHelper parkingEventOutboxHelper;
    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<Parking> parkingArgumentCaptor;

    @Captor
    private ArgumentCaptor<ParkingApprovedEvent> parkingApprovedEventArgumentCaptor;

    @InjectMocks
    private PaymentEventHandlerImpl underTest;

    @Nested
    class ProcessPaymentDebitEvent {

        @Test
        void should_ThrowParkingNotFoundException_When_ParkingNotFoundInDb() {
            //given
            PaymentDebitEvent paymentDebitEvent = createRandomDebitEvent();
            when(parkingRepository.findById(paymentDebitEvent.getParkingId())).thenReturn(Optional.empty());

            //when
            Executable executable = () -> underTest.processPaymentDebitEvent(paymentDebitEvent);

            //then
            assertThrows(ParkingNotFoundException.class, executable);
        }

        @Test
        void should_DoNothingAndReturn_When_AlreadyApproved() {
            //given
            PaymentDebitEvent paymentDebitEvent = createRandomDebitEvent();
            Parking loadedParking = makeRandomCreateParking(ParkingStatus.CREATED).build();

            when(parkingRepository.findById(any())).thenReturn(Optional.of(loadedParking));

            //when
            underTest.processPaymentDebitEvent(paymentDebitEvent);

            //then
            verify(parkingRepository).findById(paymentDebitEvent.getParkingId());
            verify(parkingRepository, never()).save(any());
            verifyNoInteractions(parkingEventOutboxHelper);
        }

        @Test
        void should_ThrowInvalidParkingStatusException() {
            //given
            PaymentDebitEvent paymentDebitEvent = createRandomDebitEvent();
            Parking loadedParking = makeRandomStopParking(ParkingStatus.STOPPED).build();

            when(parkingRepository.findById(paymentDebitEvent.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            Executable executable = () -> underTest.processPaymentDebitEvent(paymentDebitEvent);

            //given
            assertThrows(InvalidParkingStatusException.class, executable);
        }

        @Test
        void should_UpdateParkingStatusAndPersistEventAndReturnCorrectResponse() {
            //given
            PaymentDebitEvent paymentDebitEvent = createRandomDebitEvent();
            Parking loadedParking = makeRandomCreateParking(ParkingStatus.CREATE_PENDING).build();

            when(parkingRepository.findById(paymentDebitEvent.getParkingId())).thenReturn(Optional.of(loadedParking));
            when(parkingRepository.save(any(Parking.class)))
                    .thenAnswer(answer -> answer.getArgument(0));

            //when
            underTest.processPaymentDebitEvent(paymentDebitEvent);

            //then
            verify(parkingRepository).save(parkingArgumentCaptor.capture());
            verify(parkingEventOutboxHelper).persistOutboxMessageFromEvent(parkingApprovedEventArgumentCaptor.capture());

            Parking saveParkingArg = parkingArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(loadedParking.getId(), saveParkingArg.getId()),
                    () -> assertEquals(ParkingStatus.CREATED, saveParkingArg.getParkingStatus()));

            ParkingApprovedEvent parkingApprovedEvent = parkingApprovedEventArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(loadedParking.getCustomerId(), parkingApprovedEvent.getCustomerId()),
                    () -> assertEquals(loadedParking.getId(), parkingApprovedEvent.getParkingId()));
        }
    }

    @Nested
    class ProcessPaymentDebitFailedEvent {

        @Test
        void should_ThrowParkingNotFoundException_When_ParkingNotFoundInDb() {
            //given
            PaymentDebitEvent paymentDebitEvent = createRandomDebitEvent();
            when(parkingRepository.findById(paymentDebitEvent.getParkingId())).thenReturn(Optional.empty());

            //when
            Executable executable = () -> underTest.processPaymentDebitFailedEvent(paymentDebitEvent);

            //then
            assertThrows(ParkingNotFoundException.class, executable);
        }

        @Test
        void should_DoNothingAndReturn_When_AlreadyDeclined() {
            //given
            PaymentDebitEvent paymentDebitEvent = createRandomDebitEvent();
            Parking loadedParking = Parking.builder().parkingStatus(ParkingStatus.CANCELLED).build();

            when(parkingRepository.findById(paymentDebitEvent.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            underTest.processPaymentDebitFailedEvent(paymentDebitEvent);

            //then
            verify(parkingRepository).findById(paymentDebitEvent.getParkingId());
            verify(parkingRepository, never()).save(any());
        }

        @Test
        void should_ThrowInvalidParkingStatusException() {
            //given
            PaymentDebitEvent paymentDebitEvent = createRandomDebitEvent();
            Parking loadedParking = makeRandomCreateParking(ParkingStatus.CREATED).build();

            when(parkingRepository.findById(paymentDebitEvent.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            Executable executable = () -> underTest.processPaymentDebitFailedEvent(paymentDebitEvent);

            //given
            assertThrows(InvalidParkingStatusException.class, executable);
        }

        @Test
        void should_UpdateParkingStatus() {
            //given
            PaymentDebitEvent paymentDebitEvent = createRandomDebitEvent();
            Parking loadedParking = makeRandomCreateParking(ParkingStatus.CREATE_PENDING).build();

            when(parkingRepository.findById(paymentDebitEvent.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            underTest.processPaymentDebitFailedEvent(paymentDebitEvent);

            //then
            verify(parkingRepository).save(parkingArgumentCaptor.capture());

            Parking saveParkingArg = parkingArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(loadedParking.getId(), saveParkingArg.getId()),
                    () -> assertEquals(ParkingStatus.CANCELLED, saveParkingArg.getParkingStatus()));
        }
    }

    @Nested
    class ProcessPaymentRefundEvent {
        @Test
        void should_ThrowParkingNotFoundException_When_ParkingNotFoundInDb() {
            //given
            PaymentRefundEvent paymentRefundEvent = createRandomRefundEvent();
            when(parkingRepository.findById(paymentRefundEvent.getParkingId())).thenReturn(Optional.empty());

            //when
            Executable executable = () -> underTest.processPaymentRefundEvent(paymentRefundEvent);

            //then
            assertThrows(ParkingNotFoundException.class, executable);
        }

        @Test
        void should_DoNothingAndReturn_When_AlreadyStopped() {
            //given
            PaymentRefundEvent paymentRefundEvent = createRandomRefundEvent();
            Parking loadedParking = makeRandomStopParking(ParkingStatus.STOPPED).build();

            when(parkingRepository.findById(paymentRefundEvent.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            underTest.processPaymentRefundEvent(paymentRefundEvent);

            //then
            verify(parkingRepository).findById(paymentRefundEvent.getParkingId());
            verify(parkingRepository, never()).save(any());
        }

        @Test
        void should_ThrowInvalidParkingStatusException() {
            //given
            PaymentRefundEvent paymentRefundEvent = createRandomRefundEvent();
            Parking loadedParking = makeRandomCreateParking(ParkingStatus.CREATED).build();

            when(parkingRepository.findById(paymentRefundEvent.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            Executable executable = () -> underTest.processPaymentRefundEvent(paymentRefundEvent);

            //given
            assertThrows(InvalidParkingStatusException.class, executable);
        }

        @Test
        void should_UpdateParkingStatus() {
            //given
            PaymentRefundEvent paymentRefundEvent = createRandomRefundEvent();
            Parking loadedParking = makeRandomStopParking(ParkingStatus.STOP_PENDING).build();

            when(parkingRepository.findById(paymentRefundEvent.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            underTest.processPaymentRefundEvent(paymentRefundEvent);

            //then
            verify(parkingRepository).save(parkingArgumentCaptor.capture());

            Parking saveParkingArg = parkingArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(loadedParking.getId(), saveParkingArg.getId()),
                    () -> assertEquals(ParkingStatus.STOPPED, saveParkingArg.getParkingStatus()));
        }
    }

    private PaymentRefundEvent createRandomRefundEvent() {
        return PaymentRefundEvent.builder()
                .paymentId(UUID.randomUUID())
                .parkingId(UUID.randomUUID())
                .paymentStatus(PaymentRefundStatus.REFUNDED)
                .build();
    }

    private PaymentDebitEvent createRandomDebitEvent() {
        return PaymentDebitEvent.builder()
                .paymentId(UUID.randomUUID())
                .parkingId(UUID.randomUUID())
                .paymentStatus(PaymentDebitStatus.DEBITED).build();
    }

    private Parking.Builder makeRandomCreateParking(ParkingStatus createStatus) {
        return Parking.builder()
                .id(UUID.randomUUID())
                .zoneId(UUID.randomUUID())
                .trackingId(UUID.randomUUID())
                .licensePlateNumber("ABC-123")
                .customerId(UUID.randomUUID())
                .startingFee(new Money(BigDecimal.valueOf(5000)))
                .startedAt(Instant.now())
                .trackingId(UUID.randomUUID())
                .parkingStatus(createStatus);
    }

    private Parking.Builder makeRandomStopParking(ParkingStatus stopStatus) {
        return Parking.builder()
                .id(UUID.randomUUID())
                .zoneId(UUID.randomUUID())
                .trackingId(UUID.randomUUID())
                .licensePlateNumber("ABC-123")
                .customerId(UUID.randomUUID())
                .startingFee(new Money(BigDecimal.valueOf(5000)))
                .startedAt(Instant.now())
                .stoppedAt(Instant.now().plusSeconds(600))
                .closingFee(new Money(BigDecimal.valueOf(500)))
                .trackingId(UUID.randomUUID())
                .parkingStatus(stopStatus);
    }
}
