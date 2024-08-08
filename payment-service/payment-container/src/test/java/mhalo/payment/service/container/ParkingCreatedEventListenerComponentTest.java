package mhalo.payment.service.container;

import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.domain.ParkingCreatedEventListenerImpl;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommandResponse;
import mhalo.payment.service.domain.exception.DuplicateEventException;
import mhalo.payment.service.domain.ports.output.httpclient.ApplePayClient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@Testcontainers
@ActiveProfiles({"component-test", "test"})
@SpringBootTest(classes = {PaymentServiceApplication.class})
@Sql(scripts = "classpath:scripts/empty-processed-events-table.sql" ,  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ExtendWith(MockitoExtension.class)
public class ParkingCreatedEventListenerComponentTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.3"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        int port = postgres.getMappedPort(5432);
        String jdbcUrl = "jdbc:postgresql://localhost:%s/postgres?currentSchema=payment&stringtype=unspecified"
                .formatted(port);
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("kafka-config.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private ParkingCreatedEventListenerImpl parkingCreatedEventListener;

    @SpyBean
    private ApplePayClient applePayClient;

    @Nested
    class IdempotentConsumerTests {
        @Test
        void sequentialExecution_Should_ThrowDuplicateEventException_When_ConsumingDuplicateEvent() {
            //given
            ParkingCreatedEvent parkingCreatedEvent = createRandomParkingCreatedEvent(UUID.randomUUID()).build();

            //when
            parkingCreatedEventListener.process(parkingCreatedEvent);
            Executable executable = () -> parkingCreatedEventListener.process(parkingCreatedEvent);

            //then
            verify(applePayClient, times(1)).executeTransaction(any());
            assertThrows(DuplicateEventException.class, executable);
        }

        @Test
        void concurrentExecution_Should_ThrowDuplicateEventException_When_ConsumingDuplicateEvent() {
            //given
            ParkingCreatedEvent parkingCreatedEvent = createRandomParkingCreatedEvent(UUID.randomUUID()).build();

            when(applePayClient.executeTransaction(any())).thenAnswer(invocation -> {
                Thread.sleep(500);
                return ApplePayCommandResponse.builder()
                        .isPaymentSuccessful(true)
                        .providerTransactionId(UUID.randomUUID())
                        .build();
            });

            //when
            Mono<Object> monoOne = Mono.fromRunnable(() -> parkingCreatedEventListener.process(parkingCreatedEvent)).subscribeOn(Schedulers.boundedElastic());
            Mono<Object> monoTwo = Mono.fromRunnable(() -> parkingCreatedEventListener.process(parkingCreatedEvent)).subscribeOn(Schedulers.boundedElastic());

            var concurrentProcessingZip = Mono.zipDelayError(monoOne, monoTwo);

            //then
            StepVerifier.create(concurrentProcessingZip)
                    .expectError(DuplicateEventException.class)
                    .verify();

            verify(applePayClient, times(1)).executeTransaction(any());
        }
    }

    private ParkingCreatedEvent.ParkingCreatedEventBuilder createRandomParkingCreatedEvent(UUID eventId) {
        return ParkingCreatedEvent.builder()
                .eventId(eventId)
                .parkingId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .startingFee(new Money(BigDecimal.valueOf(500)));
    }
}
