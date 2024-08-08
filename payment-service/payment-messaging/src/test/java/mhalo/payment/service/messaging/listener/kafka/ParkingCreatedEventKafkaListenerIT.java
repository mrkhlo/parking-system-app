package mhalo.payment.service.messaging.listener.kafka;

import mhalo.payment.service.messaging.test.it.config.MockWrappedBeanResetTestExecutionListener;
import mhalo.payment.service.messaging.test.it.config.PaymentServiceConfiguration;
import mhalo.payment.service.messaging.test.it.config.TestBeanConfiguration;
import mhalo.kafka.config.data.KafkaConsumerConfigData;
import mhalo.kafka.producer.service.KafkaProducer;
import mhalo.parking.service.core.domain.ParkingAvroModel;
import mhalo.parking.service.core.domain.ParkingCreatedEventAvroModel;
import mhalo.payment.service.domain.config.PaymentServiceConfigData;
import mhalo.payment.service.domain.exception.DuplicateEventException;
import mhalo.payment.service.domain.exception.InvalidPaymentStatusException;
import mhalo.payment.service.domain.model.PaymentStatus;
import mhalo.payment.service.domain.ports.input.event.listener.parking.ParkingCreatedEventListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testcontainers
@ActiveProfiles({"test", "integration-test"})
@SpringBootTest(classes = {PaymentServiceConfiguration.class, TestBeanConfiguration.class})
@TestExecutionListeners(
        listeners = {
                MockWrappedBeanResetTestExecutionListener.class,
        },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Import(ParkingCreatedEventKafkaListener.class)
@ExtendWith(MockitoExtension.class)
public class ParkingCreatedEventKafkaListenerIT {

    @Autowired
    private KafkaProducer<String, ParkingCreatedEventAvroModel> kafkaProducer;

    @Autowired
    private PaymentServiceConfigData paymentServiceConfigData;

    @MockBean
    private ParkingCreatedEventListener parkingCreatedEventListener;

    @Autowired
    private KafkaConsumerConfigData kafkaConsumerConfigData;

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.3"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String bootstrapServers = kafka.getBootstrapServers();
        registry.add("kafka-config.bootstrap-servers", () -> bootstrapServers);
    }

    @Test
    void should_CallDomainListener() {
        //given
        UUID eventId = UUID.randomUUID();
        var eventAvroModel = createRandomParkingCreatedEventAvroModel();
        String partitionKey = eventAvroModel.getParking().getParkingId().toString();

        //when
        kafkaProducer.send(paymentServiceConfigData.getParkingCreatedEventTopicName(),
                partitionKey, eventAvroModel, eventId);

        //then
        verify(parkingCreatedEventListener, after(2000).only()).process(any());
    }

    @ParameterizedTest
    @MethodSource("retryableExceptionsMethodSource")
    void should_RetryConfiguredTimes_When_RetryableExceptionThrown(Throwable exception) {
        //given
        UUID eventId = UUID.randomUUID();
        var eventAvroModel = createRandomParkingCreatedEventAvroModel();
        String partitionKey = eventAvroModel.getParking().getParkingId().toString();
        doThrow(exception)
                .when(parkingCreatedEventListener).process(any());

        //when
        kafkaProducer.send(paymentServiceConfigData.getParkingCreatedEventTopicName(),
                partitionKey, eventAvroModel, eventId);

        //then
        int timeout = 2000 + kafkaConsumerConfigData.getRetryCount() * kafkaConsumerConfigData.getRetryBackoffMs();
        verify(parkingCreatedEventListener, after(timeout).times(kafkaConsumerConfigData.getRetryCount() + 1)).process(any());
    }

    @ParameterizedTest
    @MethodSource("noopExceptionsMethodSource")
    void should_CallOnceAndDoNothingAndNotRetry_When_NoopException(Throwable exception) {
        //given
        UUID eventId = UUID.randomUUID();
        var eventAvroModel = createRandomParkingCreatedEventAvroModel();
        String partitionKey = eventAvroModel.getParking().getParkingId().toString();
        doThrow(exception)
                .when(parkingCreatedEventListener).process(any());

        //when
        kafkaProducer.send(paymentServiceConfigData.getParkingCreatedEventTopicName(),
                partitionKey, eventAvroModel, eventId);

        //then
        verify(parkingCreatedEventListener, after(2000).only()).process(any());
    }

    private static Stream<Throwable> retryableExceptionsMethodSource() {
        SQLException sqlException = new SQLException("something", PSQLState.NO_DATA.getState());
        DataIntegrityViolationException violationException = new DataIntegrityViolationException("something", sqlException);
        return Stream.of(
                violationException
        );
    }

    private static Stream<Throwable> noopExceptionsMethodSource() {
        SQLException sqlException = new SQLException("something", PSQLState.UNIQUE_VIOLATION.getState());
        DataIntegrityViolationException violationException = new DataIntegrityViolationException("something", sqlException);
        return Stream.of(
                violationException,
                new InvalidPaymentStatusException(PaymentStatus.DEBITED, PaymentStatus.REFUNDED, "something"),
                new DuplicateEventException(UUID.randomUUID())
        );
    }

    private ParkingCreatedEventAvroModel createRandomParkingCreatedEventAvroModel() {
        ParkingCreatedEventAvroModel event = new ParkingCreatedEventAvroModel();
        event.setParking(createRandomParking());
        return event;
    }

    private ParkingAvroModel createRandomParking() {
        ParkingAvroModel parkingAvroModel = new ParkingAvroModel();
        parkingAvroModel.setParkingId(UUID.randomUUID());
        parkingAvroModel.setZoneId(UUID.randomUUID());
        parkingAvroModel.setCustomerId(UUID.randomUUID());
        parkingAvroModel.setTrackingId(UUID.randomUUID());
        parkingAvroModel.setLicensePlateNumber("ABC123");
        parkingAvroModel.setStartingFee(BigDecimal.TEN);
        parkingAvroModel.setStartedAt(Instant.now());
        parkingAvroModel.setStoppedAt(Instant.now());

        return parkingAvroModel;
    }
}
