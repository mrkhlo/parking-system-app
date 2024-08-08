package mhalo.payment.service.dataaccess.adapter;

import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.dataaccess.entity.PaymentEntity;
import mhalo.payment.service.dataaccess.entity.TransactionEntity;
import mhalo.payment.service.dataaccess.repository.PaymentJpaRepository;
import mhalo.payment.service.dataaccess.test.it.config.PaymentServiceConfiguration;
import mhalo.payment.service.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ActiveProfiles({"test", "integration-test"})
@SpringBootTest(classes = PaymentServiceConfiguration.class)
class PaymentRepositoryIT {

    @Autowired
    private PaymentRepositoryImpl paymentRepository;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

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
    void should_SavePayment() {
        //given
        Payment payment = createPayment();

        //when
        paymentRepository.save(payment);

        //then
        Optional<PaymentEntity> parkingOpt = paymentJpaRepository.findByParkingId(payment.getParkingId());
        assertTrue(parkingOpt.isPresent());
    }

    @Test
    @Transactional
    void should_FindPaymentByParkingId() {
        //given
        PaymentEntity payment = createPaymentEntity();
        paymentJpaRepository.save(payment);

        //when
        Optional<Payment> parkingOpt = paymentRepository.findByParkingId(payment.getParkingId());

        //then
        assertTrue(parkingOpt.isPresent());
    }

    @Test
    @Transactional
    void should_ReturnEmptyOptional_When_PaymentNotFoundByParkingId() {
        //given
        //when
        Optional<Payment> parkingOpt = paymentRepository.findByParkingId(UUID.randomUUID());

        //then
        assertTrue(parkingOpt.isEmpty());
    }

    private Payment createPayment() {
        return Payment.builder()
                .parkingId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.DEBITED)
                .transactions(List.of(Transaction.builder()
                        .amount(Money.ZERO)
                        .transactionStatus(TransactionStatus.SUCCESS)
                        .transactionType(TransactionType.DEBIT)
                        .id(UUID.randomUUID())
                        .executedAt(Instant.now())
                        .providerTransactionId(UUID.randomUUID()).build()))
                .build();
    }

    private PaymentEntity createPaymentEntity() {
        TransactionEntity transaction = TransactionEntity.builder()
                .amount(BigDecimal.ZERO)
                .transactionStatus(TransactionStatus.SUCCESS)
                .transactionType(TransactionType.DEBIT)
                .id(UUID.randomUUID())
                .executedAt(Instant.now())
                .providerTransactionId(UUID.randomUUID()).build();
        PaymentEntity payment = PaymentEntity.builder()
                .parkingId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.DEBITED)
                .transactions(List.of(transaction))
                .build();
        transaction.setPayment(payment);
        return payment;
    }
}
