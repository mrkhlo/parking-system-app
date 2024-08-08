package mhalo.parking.service.domain;

import mhalo.domain.model.event.DomainConstants;
import mhalo.domain.model.event.model.Interval;
import mhalo.domain.model.event.model.Money;
import mhalo.domain.model.event.model.Rate;
import mhalo.parking.service.domain.dto.rest.start.StartParkingCommand;
import mhalo.parking.service.domain.dto.rest.start.StartParkingResponse;
import mhalo.parking.service.domain.dto.rest.stop.StopParkingCommand;
import mhalo.parking.service.domain.dto.rest.track.TrackParkingResponse;
import mhalo.parking.service.domain.exception.InvalidParkingStatusException;
import mhalo.parking.service.domain.exception.ParkingDomainException;
import mhalo.parking.service.domain.exception.ParkingNotFoundException;
import mhalo.parking.service.domain.exception.ParkingStartedInFreeZone;
import mhalo.parking.service.domain.mapper.ParkingDataMapper;
import mhalo.parking.service.domain.model.Parking;
import mhalo.parking.service.domain.model.ParkingStatus;
import mhalo.parking.service.domain.model.Zone;
import mhalo.parking.service.domain.model.event.ParkingApprovedEvent;
import mhalo.parking.service.domain.model.event.ParkingCreatedEvent;
import mhalo.parking.service.domain.model.event.ParkingStoppedEvent;
import mhalo.parking.service.domain.outbox.scheduler.ParkingEventOutboxHelper;
import mhalo.parking.service.domain.ports.output.httpclient.CustomerRestClient;
import mhalo.parking.service.domain.ports.output.httpclient.ZoneRestClient;
import mhalo.parking.service.domain.ports.output.repository.ParkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * For simplicity tested with {@link ParkingDomainService}
 */
@ExtendWith(MockitoExtension.class)
class TestParkingApplicationServiceImpl {

    @Mock
    private CustomerRestClient customerRestClient;
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
    private ArgumentCaptor<ParkingCreatedEvent> parkingCreatedEventArgumentCaptor;

    @Captor
    private ArgumentCaptor<ParkingApprovedEvent> parkingApprovedEventArgumentCaptor;

    @Captor
    private ArgumentCaptor<ParkingStoppedEvent> parkingStoppedEventArgumentCaptor;

    @BeforeEach
    void prepare() {
    }

    @InjectMocks
    private ParkingApplicationServiceImpl underTest;

    @Nested
    class StartParking {

         @Test
        void should_ThrowParkingDomainException_When_CustomerNotFound() {
            //given
            StartParkingCommand startParkingCommand = createRandomStartCommand();
            when(customerRestClient.isCustomerExistsById(startParkingCommand.getCustomerId()))
                    .thenReturn(false);
            //when
            Executable executable = () -> underTest.startParking(startParkingCommand);

            //then
            assertThrows(ParkingDomainException.class, executable);
        }

        @Test
        void should_ThrowParkingDomainException_When_ZoneInFreeInterval() {
            //given
            StartParkingCommand startParkingCommand = createRandomStartCommand();
            Zone parkingZone = createRandomZone().id(startParkingCommand.getZoneId())
                    .payInterval(new Interval(LocalTime.of(10, 0), LocalTime.of(20, 0))).build();

            LocalTime parkingStartTime = LocalTime.of(8, 0);
            Instant parkingStartAt =  ZonedDateTime.of(LocalDate.of(2010,1,1), parkingStartTime,
                    ZoneId.of(DomainConstants.UTC)).toInstant();

            when(clock.instant()).thenReturn(parkingStartAt);
            when(customerRestClient.isCustomerExistsById(startParkingCommand.getCustomerId())).thenReturn(true);
            when(zoneRestClient.getZoneById(startParkingCommand.getZoneId())).thenReturn(parkingZone);

            //when
            Executable executable = () -> underTest.startParking(startParkingCommand);

            //then
            assertThrows(ParkingStartedInFreeZone.class, executable);
        }

        @Test
        void should_PersistParkingWithEventAndReturnCorrectResponse() {
            //given
            StartParkingCommand startParkingCommand = createRandomStartCommand();
            Zone parkingZone = createRandomZone().id(startParkingCommand.getZoneId())
                    .payInterval(new Interval(LocalTime.of(10, 0), LocalTime.of(20, 0))).build();
            Money startingFee = new Money(BigDecimal.valueOf(500));

            LocalTime nowTime = LocalTime.of(14, 0);
            Instant parkingStartTime =  ZonedDateTime.of(LocalDate.of(2010,1,1), nowTime, 
                    ZoneId.of(DomainConstants.UTC)).toInstant();

            when(clock.instant()).thenReturn(parkingStartTime);
            when(customerRestClient.isCustomerExistsById(startParkingCommand.getCustomerId())).thenReturn(true);
            when(zoneRestClient.getZoneById(startParkingCommand.getZoneId())).thenReturn(parkingZone);
            when(parkingFeeCalculator.calculateStartingFee(eq(parkingStartTime), any())).thenReturn(startingFee);
            when(parkingRepository.save(any(Parking.class)))
                    .thenAnswer(answer -> answer.getArgument(0));

            //when
            StartParkingResponse startParkingResponse = underTest.startParking(startParkingCommand);

            //then
            verify(parkingRepository).save(parkingArgumentCaptor.capture());
            verify(parkingEventOutboxHelper).persistOutboxMessageFromEvent(parkingCreatedEventArgumentCaptor.capture());

            Parking saveParkingArg = parkingArgumentCaptor.getValue();
            assertAll(
                    () -> assertNotNull(saveParkingArg.getId()),
                    () -> assertNotNull(saveParkingArg.getTrackingId()),
                    () -> assertEquals(startParkingCommand.getZoneId(), saveParkingArg.getZoneId()),
                    () -> assertEquals(startParkingCommand.getCustomerId(), saveParkingArg.getCustomerId()),
                    () -> assertEquals(startParkingCommand.getLicensePlateNumber(), saveParkingArg.getLicensePlateNumber()),
                    () -> assertEquals(parkingStartTime, saveParkingArg.getStartedAt()),
                    () -> assertEquals(ParkingStatus.CREATE_PENDING, saveParkingArg.getParkingStatus()),
                    () -> assertEquals(startingFee, saveParkingArg.getStartingFee()));

            ParkingCreatedEvent parkingCreatedEventArg = parkingCreatedEventArgumentCaptor.getValue();
            assertAll(
                    () -> assertNotNull(parkingCreatedEventArg.getParking()),
                    () -> assertThat(saveParkingArg).usingRecursiveComparison().isEqualTo(parkingCreatedEventArg.getParking()));

            assertAll(
                    () -> assertEquals(saveParkingArg.getId(), startParkingResponse.getParkingId()),
                    () -> assertEquals(saveParkingArg.getTrackingId(), startParkingResponse.getParkingTrackingId()));
        }
    }

    @Nested
    class StopParking {
        @Test
        void should_DoNothingAndReturn_When_ParkingAlreadyStopped() {
            //given
            StopParkingCommand stopParkingCommand = new StopParkingCommand(UUID.randomUUID());
            Parking loadedParking = makeRandomStopParking(ParkingStatus.STOP_PENDING).id(stopParkingCommand.getParkingId()).build();
            when(parkingRepository.findById(stopParkingCommand.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            underTest.stopParking(stopParkingCommand);

            //then
            verify(parkingRepository, never()).save(any());
            verifyNoInteractions(parkingEventOutboxHelper);
        }

        @Test
        void should_ThrowInvalidParkingStatusException() {
            //given
            StopParkingCommand stopParkingCommand = new StopParkingCommand(UUID.randomUUID());
            Parking loadedParking = makeRandomCreateParking(ParkingStatus.CREATE_PENDING).id(stopParkingCommand.getParkingId()).build();
            Zone parkingZone = createRandomZone().id(loadedParking.getZoneId()).build();
            Instant stoppedAt = Instant.now();
            when(clock.instant()).thenReturn(stoppedAt);
            when(zoneRestClient.getZoneById(loadedParking.getZoneId())).thenReturn(parkingZone);
            when(parkingFeeCalculator.calculateClosingFee(any(), eq(loadedParking.getStartedAt()), eq(stoppedAt), eq(loadedParking.getStartingFee())))
                    .thenReturn(new Money(BigDecimal.valueOf(500)));
            when(parkingRepository.findById(stopParkingCommand.getParkingId())).thenReturn(Optional.of(loadedParking));

            //when
            Executable executable = () -> underTest.stopParking(stopParkingCommand);

            //then
            assertThrows(InvalidParkingStatusException.class, executable);
        }

        @MethodSource("stopParkingSuccessTestArguments")
        @ParameterizedTest(name = "{index} {2} status is expected for startingFee: {0} and closingFee: {1} ")
        void should_PersistStoppedParkingAndEvent(Money startingFee, Money closingFee, ParkingStatus expectedNewStatus) {
            //given
            StopParkingCommand stopParkingCommand = new StopParkingCommand(UUID.randomUUID());
            Instant stoppedAt = Instant.now();

            Parking loadedParking = Parking.builder()
                    .id(stopParkingCommand.getParkingId())
                    .zoneId(UUID.randomUUID())
                    .trackingId(UUID.randomUUID())
                    .licensePlateNumber("ABC-123")
                    .customerId(UUID.randomUUID())
                    .startingFee(startingFee)
                    .startedAt(stoppedAt.minusSeconds(600))
                    .trackingId(stopParkingCommand.getParkingId())
                    .parkingStatus(ParkingStatus.CREATED).build();
            Zone parkingZone = createRandomZone().id(loadedParking.getZoneId()).build();

            when(clock.instant()).thenReturn(stoppedAt);
            when(zoneRestClient.getZoneById(loadedParking.getZoneId())).thenReturn(parkingZone);
            when(parkingFeeCalculator.calculateClosingFee(any(), eq(loadedParking.getStartedAt()), eq(stoppedAt), eq(loadedParking.getStartingFee())))
                    .thenReturn(closingFee);
            when(parkingRepository.findById(stopParkingCommand.getParkingId())).thenReturn(Optional.of(loadedParking));
            when(parkingRepository.save(any(Parking.class)))
                    .thenAnswer(answer -> answer.getArgument(0));

            //when
            underTest.stopParking(stopParkingCommand);

            //then
            verify(parkingRepository).save(parkingArgumentCaptor.capture());
            verify(parkingEventOutboxHelper).persistOutboxMessageFromEvent(parkingStoppedEventArgumentCaptor.capture());

            Parking saveParkingArg = parkingArgumentCaptor.getValue();
            assertAll(
                    () -> assertThat(loadedParking).usingRecursiveComparison()
                            .ignoringFields("stoppedAt", "closingFee", "parkingStatus")
                            .isEqualTo(saveParkingArg),
                    () -> assertEquals(expectedNewStatus, saveParkingArg.getParkingStatus()),
                    () -> assertEquals(stoppedAt, saveParkingArg.getStoppedAt()),
                    () -> assertEquals(closingFee, saveParkingArg.getClosingFee()));

            ParkingStoppedEvent parkingStoppedEventArg = parkingStoppedEventArgumentCaptor.getValue();
            assertThat(saveParkingArg).usingRecursiveComparison().isEqualTo(parkingStoppedEventArg.getParking());
        }

        private static Stream<Arguments> stopParkingSuccessTestArguments() {
            return Stream.of(
                    Arguments.of(new Money(BigDecimal.valueOf(5000)), new Money(BigDecimal.valueOf(500)), ParkingStatus.STOP_PENDING),
                    Arguments.of(new Money(BigDecimal.valueOf(500)), new Money(BigDecimal.valueOf(500)), ParkingStatus.STOPPED));
        }
    }

    @Nested
    class TrackParking {
        @Test
        void should_ThrowParkingNotFoundException_When_ParkingNotFoundInDb() {
            //given
            UUID parkingTrackingId = UUID.randomUUID();
            when(parkingRepository.findByTrackingId(parkingTrackingId)).thenReturn(Optional.empty());

            //when
            Executable executable = () -> underTest.trackParking(parkingTrackingId);

            //then
            assertThrows(ParkingNotFoundException.class, executable);
        }

        @Test
        void should_ReturnParkingStatus() {
            //given
            UUID parkingTrackingId = UUID.randomUUID();
            Parking loadedParking = makeRandomCreateParking(ParkingStatus.CREATE_PENDING).trackingId(parkingTrackingId).build();
            when(parkingRepository.findByTrackingId(parkingTrackingId)).thenReturn(Optional.of(loadedParking));

            //when
            TrackParkingResponse actualTrackParkingResponse = underTest.trackParking(parkingTrackingId);

            //then
            assertNotNull(actualTrackParkingResponse);
            assertEquals(parkingTrackingId, actualTrackParkingResponse.getParkingTrackId());
            assertNotNull(actualTrackParkingResponse.getParkingStatus());
        }
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

    private Zone.Builder createRandomZone() {
        return Zone.builder()
                .id(UUID.randomUUID())
                .payInterval(new Interval(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                .rate(Rate.builder().duration(Duration.ofHours(1)).amount(new Money(BigDecimal.valueOf(500))).build());
    }

    private StartParkingCommand createRandomStartCommand() {
        return StartParkingCommand.builder()
                .zoneId(UUID.randomUUID())
                .licensePlateNumber("ABC-123")
                .customerId(UUID.randomUUID())
                .build();
    }
}
