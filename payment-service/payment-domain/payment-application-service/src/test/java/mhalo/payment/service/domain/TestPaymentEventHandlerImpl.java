package mhalo.payment.service.domain;

import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.dto.event.ParkingStoppedEvent;
import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommandResponse;
import mhalo.payment.service.domain.exception.PaymentNotFoundException;
import mhalo.payment.service.domain.mapper.PaymentDomainDataFactory;
import mhalo.payment.service.domain.model.*;
import mhalo.payment.service.domain.model.event.PaymentDebitEvent;
import mhalo.payment.service.domain.model.event.PaymentRefundEvent;
import mhalo.payment.service.domain.outbox.scheduler.PaymentOutboxHelper;
import mhalo.payment.service.domain.ports.output.httpclient.ApplePayClient;
import mhalo.payment.service.domain.ports.output.repository.PaymentRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestPaymentEventHandlerImpl {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ApplePayClient applePayClient;
    @Mock
    private PaymentOutboxHelper paymentOutboxHelper;
    @Spy
    private PaymentDomainDataFactory paymentDomainDataFactory;
    @InjectMocks
    private PaymentDomainService paymentDomainService = Mockito.spy(new PaymentDomainService());
    @InjectMocks
    private PaymentEventHandlerImpl underTest;

    @Spy
    private Clock clock = Clock.systemUTC();

    @Captor
    private ArgumentCaptor<Payment> paymentArgumentCaptor;
    @Captor
    private ArgumentCaptor<PaymentDebitEvent> paymentDebitEventArgumentCaptor;
    @Captor
    private ArgumentCaptor<PaymentRefundEvent> paymentRefundEventArgumentCaptor;

    @Nested
    class ProcessParkingCreatedEvent {

        @Test
        void should_DoNothingAndReturn_When_PaymentAlreadyCreated() {
            //given
            ParkingCreatedEvent parkingCreatedEvent = createRandomParkingCreatedEvent().build();
            Payment loadedPayment = creatRandomDebitedPayment().build();
            when(paymentRepository.findByParkingId(parkingCreatedEvent.getParkingId())).thenReturn(Optional.of(loadedPayment));

            //when
            underTest.processParkingCreatedEvent(parkingCreatedEvent);

            //then
            verify(paymentRepository, never()).save(any());
            verifyNoInteractions(paymentOutboxHelper);
        }

        @Test
        void should_PersistPaymentWithEventAndChargeCustomer_When_ChargeCustomerSuccess() {
            //given
            ParkingCreatedEvent parkingCreatedEvent = createRandomParkingCreatedEvent().build();
            ApplePayCommandResponse paymentResponse = ApplePayCommandResponse.builder()
                    .isPaymentSuccessful(true)
                    .providerTransactionId(UUID.randomUUID()).build();
            when(paymentRepository.findByParkingId(parkingCreatedEvent.getParkingId())).thenReturn(Optional.empty());
            when(applePayClient.executeTransaction(any())).thenReturn(paymentResponse);
            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(answer -> answer.getArgument(0));

            //when
            underTest.processParkingCreatedEvent(parkingCreatedEvent);

            //then
            verify(paymentRepository).save(paymentArgumentCaptor.capture());
            verify(paymentOutboxHelper).persistOutboxMessageFromEvent(paymentDebitEventArgumentCaptor.capture());

            Payment savedPaymentArg = paymentArgumentCaptor.getValue();
            Optional<Transaction> debitSuccessTransactionOpt = savedPaymentArg.getSuccessfulFeeChargeTransaction();
            assertTrue(debitSuccessTransactionOpt.isPresent());
            assertAll(
                    () -> assertNotNull(savedPaymentArg.getId()),
                    () -> assertEquals(parkingCreatedEvent.getParkingId(), savedPaymentArg.getParkingId()),
                    () -> assertEquals(parkingCreatedEvent.getCustomerId(), savedPaymentArg.getCustomerId()),
                    () -> assertEquals(PaymentStatus.DEBITED, savedPaymentArg.getPaymentStatus()),
                    () -> assertEquals(parkingCreatedEvent.getStartingFee(), debitSuccessTransactionOpt.get().getAmount()));

            PaymentDebitEvent paymentDebitEventArg = paymentDebitEventArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(savedPaymentArg.getId(), paymentDebitEventArg.getPaymentId()),
                    () -> assertEquals(savedPaymentArg.getCustomerId(), paymentDebitEventArg.getCustomerId()),
                    () -> assertEquals(savedPaymentArg.getParkingId(), paymentDebitEventArg.getParkingId()),
                    () -> assertEquals(savedPaymentArg.getPaymentStatus(), paymentDebitEventArg.getPaymentStatus()),
                    () -> assertThat(paymentDebitEventArg.getTransaction())
                            .usingRecursiveComparison()
                            .isEqualTo(debitSuccessTransactionOpt.get()));
        }

        @Test
        void should_PersistPaymentWithEventAndChargeCustomer_When_ChargeCustomerFails() {
            //given
            ParkingCreatedEvent parkingCreatedEvent = createRandomParkingCreatedEvent().build();
            ApplePayCommandResponse paymentResponse = ApplePayCommandResponse.builder()
                    .isPaymentSuccessful(false)
                    .providerTransactionId(UUID.randomUUID()).build();
            when(paymentRepository.findByParkingId(parkingCreatedEvent.getParkingId())).thenReturn(Optional.empty());
            when(applePayClient.executeTransaction(any())).thenReturn(paymentResponse);
            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(answer -> answer.getArgument(0));

            //when
            underTest.processParkingCreatedEvent(parkingCreatedEvent);

            //then
            verify(paymentRepository).save(paymentArgumentCaptor.capture());
            verify(paymentOutboxHelper).persistOutboxMessageFromEvent(paymentDebitEventArgumentCaptor.capture());

            Payment savedPaymentArg = paymentArgumentCaptor.getValue();
            Optional<Transaction> debitFailedTransactionOpt = savedPaymentArg.getTransactions().stream()
                    .filter(t -> t.getProviderTransactionId().equals(paymentResponse.getProviderTransactionId()) &&
                            t.getTransactionType() == TransactionType.DEBIT &&
                            t.getTransactionStatus() == TransactionStatus.FAILURE)
                    .findAny();
            assertTrue(debitFailedTransactionOpt.isPresent());
            assertAll(
                    () -> assertNotNull(savedPaymentArg.getId()),
                    () -> assertEquals(parkingCreatedEvent.getParkingId(), savedPaymentArg.getParkingId()),
                    () -> assertEquals(parkingCreatedEvent.getCustomerId(), savedPaymentArg.getCustomerId()),
                    () -> assertEquals(PaymentStatus.DEBIT_FAILED, savedPaymentArg.getPaymentStatus()),
                    () -> assertEquals(parkingCreatedEvent.getStartingFee(), debitFailedTransactionOpt.get().getAmount()));

            PaymentDebitEvent paymentDebitEventArg = paymentDebitEventArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(savedPaymentArg.getId(), paymentDebitEventArg.getPaymentId()),
                    () -> assertEquals(savedPaymentArg.getCustomerId(), paymentDebitEventArg.getCustomerId()),
                    () -> assertEquals(savedPaymentArg.getParkingId(), paymentDebitEventArg.getParkingId()),
                    () -> assertEquals(savedPaymentArg.getPaymentStatus(), paymentDebitEventArg.getPaymentStatus()),
                    () -> assertThat(paymentDebitEventArg.getTransaction())
                            .usingRecursiveComparison()
                            .isEqualTo(debitFailedTransactionOpt.get()));
        }
    }

    @Nested
    class ProcessParkingStoppedEvent {

        @Test
        void should_ThrowPaymentNotFoundException_When_NotFoundInDbByParkingId() {
            //given
            ParkingStoppedEvent parkingStoppedEvent = createRandomParkingStoppedEvent().build();
            when(paymentRepository.findByParkingId(parkingStoppedEvent.getParkingId())).thenReturn(Optional.empty());

            //when
            Executable executable = () -> underTest.processParkingStoppedEvent(parkingStoppedEvent);

            //then
            assertThrows(PaymentNotFoundException.class, executable);
        }

        @Test
        void should_DoNothingAndReturn_When_ParkingAlreadyRefunded() {
            //given
            ParkingStoppedEvent parkingStoppedEvent = createRandomParkingStoppedEvent().build();
            Payment loadedPayment = creatRandomRefundPayment().parkingId(parkingStoppedEvent.getParkingId()).build();

            when(paymentRepository.findByParkingId(parkingStoppedEvent.getParkingId())).thenReturn(Optional.of(loadedPayment));

            //when
            underTest.processParkingStoppedEvent(parkingStoppedEvent);

            //then
            verify(paymentRepository, never()).save(any());
            verifyNoInteractions(paymentOutboxHelper);
        }

        @Test
        void should_PersistUpdatedPayment_When_RefundedNoop() {
            //given
            Payment loadedPayment = creatRandomDebitedPayment().build();
            Money startingFee = loadedPayment.getSuccessfulFeeChargeTransaction().get().getAmount();
            ParkingStoppedEvent parkingStoppedEvent = createRandomParkingStoppedEvent()
                    .parkingId(loadedPayment.getParkingId()).closingFee(startingFee).build();

            when(paymentRepository.findByParkingId(loadedPayment.getParkingId())).thenReturn(Optional.of(loadedPayment));

            //when
            underTest.processParkingStoppedEvent(parkingStoppedEvent);

            //then
            verify(paymentRepository).save(paymentArgumentCaptor.capture());
            verifyNoInteractions(paymentOutboxHelper);
            verifyNoInteractions(applePayClient);

            Payment savedPaymentArg = paymentArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(loadedPayment.getId(), savedPaymentArg.getId()),
                    () -> assertEquals(PaymentStatus.REFUND_NOOP, savedPaymentArg.getPaymentStatus()));
        }

        @Test
        void should_PersistUpdatedPaymentWithEventAndRefundFee_When_RefundSuccessful() {
            //given
            Transaction debitTransaction = createRandomTransaction(TransactionType.DEBIT, TransactionStatus.SUCCESS)
                    .amount(new Money(BigDecimal.valueOf(500))).build();
            Payment loadedPayment = creatRandomDebitedPayment().transactions(List.of(debitTransaction)).build();
            ParkingStoppedEvent parkingStoppedEvent = createRandomParkingStoppedEvent().
                    closingFee(new Money(BigDecimal.valueOf(200))).parkingId(loadedPayment.getParkingId()).build();
            ApplePayCommandResponse paymentResponse = ApplePayCommandResponse.builder()
                    .isPaymentSuccessful(true)
                    .providerTransactionId(UUID.randomUUID()).build();

            when(paymentRepository.findByParkingId(loadedPayment.getParkingId())).thenReturn(Optional.of(loadedPayment));
            when(applePayClient.executeTransaction(any())).thenReturn(paymentResponse);

            //when
            underTest.processParkingStoppedEvent(parkingStoppedEvent);

            //then
            verify(paymentRepository).save(paymentArgumentCaptor.capture());
            verify(paymentOutboxHelper).persistOutboxMessageFromEvent(paymentRefundEventArgumentCaptor.capture());

            Payment savedPaymentArg = paymentArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(loadedPayment.getParkingId(), savedPaymentArg.getParkingId()),
                    () -> assertEquals(PaymentStatus.REFUNDED, savedPaymentArg.getPaymentStatus()));
            PaymentRefundEvent savedPaymentRefundEventArg = paymentRefundEventArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(savedPaymentArg.getParkingId(), savedPaymentRefundEventArg.getParkingId()),
                    () -> assertEquals(PaymentStatus.REFUNDED, savedPaymentRefundEventArg.getPaymentStatus()));
        }

        @Test
        void should_PersistUpdatedPaymentWithEventAndRefundFee_When_RefundFailed() {
            //given
            Transaction debitTransaction = createRandomTransaction(TransactionType.DEBIT, TransactionStatus.SUCCESS)
                    .amount(new Money(BigDecimal.valueOf(500))).build();
            Payment loadedPayment = creatRandomDebitedPayment().transactions(List.of(debitTransaction)).build();
            ParkingStoppedEvent parkingStoppedEvent = createRandomParkingStoppedEvent().
                    closingFee(new Money(BigDecimal.valueOf(200))).parkingId(loadedPayment.getParkingId()).build();
            ApplePayCommandResponse paymentResponse = ApplePayCommandResponse.builder()
                    .isPaymentSuccessful(false)
                    .providerTransactionId(UUID.randomUUID()).build();

            when(paymentRepository.findByParkingId(loadedPayment.getParkingId())).thenReturn(Optional.of(loadedPayment));
            when(applePayClient.executeTransaction(any())).thenReturn(paymentResponse);

            //when
            underTest.processParkingStoppedEvent(parkingStoppedEvent);

            //then
            verify(paymentRepository).save(paymentArgumentCaptor.capture());
            verify(paymentOutboxHelper).persistOutboxMessageFromEvent(paymentRefundEventArgumentCaptor.capture());

            Payment savedPaymentArg = paymentArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(loadedPayment.getParkingId(), savedPaymentArg.getParkingId()),
                    () -> assertEquals(PaymentStatus.REFUND_FAILED, savedPaymentArg.getPaymentStatus()));
            PaymentRefundEvent savedPaymentRefundEventArg = paymentRefundEventArgumentCaptor.getValue();
            assertAll(
                    () -> assertEquals(savedPaymentArg.getParkingId(), savedPaymentRefundEventArg.getParkingId()),
                    () -> assertEquals(PaymentStatus.REFUND_FAILED, savedPaymentRefundEventArg.getPaymentStatus()));
        }

    }

    private ParkingCreatedEvent.ParkingCreatedEventBuilder createRandomParkingCreatedEvent() {
        return ParkingCreatedEvent.builder()
                .parkingId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .startingFee(new Money(BigDecimal.valueOf(500)));
    }

    private ParkingStoppedEvent.ParkingStoppedEventBuilder createRandomParkingStoppedEvent() {
        return ParkingStoppedEvent.builder()
                .parkingId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .stoppedAt(Instant.now())
                .startedAt(Instant.now())
                .closingFee(new Money(BigDecimal.valueOf(500)));
    }

    private Payment.Builder creatRandomDebitedPayment() {
        return Payment.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.DEBITED)
                .transactions(List.of(createRandomTransaction(TransactionType.DEBIT, TransactionStatus.SUCCESS).build()))
                .parkingId(UUID.randomUUID())
                .customerId(UUID.randomUUID());
    }

    private Payment.Builder creatRandomRefundPayment() {
        List<Transaction> transactions = List.of(
                createRandomTransaction(TransactionType.DEBIT, TransactionStatus.SUCCESS).build(),
                createRandomTransaction(TransactionType.CREDIT, TransactionStatus.SUCCESS).build());
        return Payment.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.REFUNDED)
                .transactions(transactions)
                .parkingId(UUID.randomUUID())
                .customerId(UUID.randomUUID());
    }

    private Transaction.Builder createRandomTransaction(TransactionType transactionType, TransactionStatus transactionStatus) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .executedAt(Instant.now())
                .transactionType(transactionType)
                .transactionStatus(transactionStatus)
                .providerTransactionId(UUID.randomUUID())
                .amount(new Money(BigDecimal.valueOf(500)));
    }

}
