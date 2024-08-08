package mhalo.payment.service.dataaccess.outbox.adapter;

import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.dataaccess.outbox.entity.PaymentEventOutboxMessageEntity;
import mhalo.payment.service.dataaccess.outbox.repository.PaymentOutboxJpaRepository;
import mhalo.payment.service.dataaccess.test.it.config.PaymentServiceConfiguration;
import mhalo.payment.service.domain.model.event.PaymentEventType;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@ActiveProfiles({"test", "integration-test"})
@SpringBootTest(classes = PaymentServiceConfiguration.class)
class PaymentOutboxRepositoryIT {

    @Autowired
    private PaymentOutboxRepositoryImpl paymentOutboxRepository;

    @Autowired
    private PaymentOutboxJpaRepository paymentOutboxJpaRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        int port = postgres.getMappedPort(5432);
        String jdbcUrl = "jdbc:postgresql://localhost:%s/postgres?currentSchema=payment&stringtype=unspecified"
                .formatted(port);
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @Transactional
    void should_SavePaymentEventOutboxMessage() {
        //given
        PaymentEventOutboxMessage paymentEventOutboxMessage = createPaymentEventOutboxMessage();

        //when
        paymentOutboxRepository.save(paymentEventOutboxMessage);

        //then
        List<PaymentEventOutboxMessageEntity> retrievedEntries =
                paymentOutboxJpaRepository.findPaymentOutboxEntitiesByOutboxStatus(OutboxStatus.STARTED);
        assertEquals(1, retrievedEntries.size());
        assertEquals(paymentEventOutboxMessage.getPaymentId(), retrievedEntries.get(0).getPaymentId());
    }

    @Test
    @Transactional
    @Sql(scripts = "classpath:scripts/insert-two-random-payment-event-outbox-message.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void should_findPaymentEventOutboxMessagesByOutboxStatus() {
        //given
        //when
        List<PaymentEventOutboxMessage> paymentEventOutboxMessagesByOutboxStatus =
                paymentOutboxRepository.getPaymentEventOutboxMessagesByOutboxStatus(OutboxStatus.STARTED);

        //then
        assertEquals(2, paymentEventOutboxMessagesByOutboxStatus.size());

        for (PaymentEventOutboxMessage paymentEventOutboxMessage : paymentEventOutboxMessagesByOutboxStatus) {
            assertEquals(OutboxStatus.STARTED, paymentEventOutboxMessage.getOutboxStatus());
        }
    }

    public static PaymentEventOutboxMessage createPaymentEventOutboxMessage() {
        return PaymentEventOutboxMessage.builder()
                .id(UUID.randomUUID())
                .paymentId(UUID.randomUUID())
                .parkingId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .createdAt(Instant.now())
                .processedAt(null)
                .payload("{\"example\": \"payload\"}")
                .outboxStatus(OutboxStatus.STARTED)
                .paymentEventType(PaymentEventType.REFUND)
                .version(1)
                .build();
    }
}
