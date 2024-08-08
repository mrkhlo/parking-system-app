package mhalo.payment.service.domain;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.domain.model.event.model.Money;
import mhalo.domain.model.event.model.ResultWithDomainEvent;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.dto.event.ParkingStoppedEvent;
import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommand;
import mhalo.payment.service.domain.dto.httpclient.apple.pay.ApplePayCommandResponse;
import mhalo.payment.service.domain.exception.PaymentNotFoundException;
import mhalo.payment.service.domain.mapper.PaymentDomainDataFactory;
import mhalo.payment.service.domain.model.Payment;
import mhalo.payment.service.domain.model.TransactionType;
import mhalo.payment.service.domain.model.event.PaymentDebitEvent;
import mhalo.payment.service.domain.model.event.PaymentRefundEvent;
import mhalo.payment.service.domain.outbox.scheduler.PaymentOutboxHelper;
import mhalo.payment.service.domain.dto.CreatePaymentDetails;
import mhalo.payment.service.domain.dto.RefundPaymentDetails;
import mhalo.payment.service.domain.ports.input.event.handler.parking.PaymentEventHandler;
import mhalo.payment.service.domain.ports.output.httpclient.ApplePayClient;
import mhalo.payment.service.domain.ports.output.repository.PaymentRepository;
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
public class PaymentEventHandlerImpl implements PaymentEventHandler {

    private final PaymentRepository paymentRepository;
    private final PaymentDomainService paymentDomainService;
    private final ApplePayClient applePayClient;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final PaymentDomainDataFactory paymentDomainDataFactory;
    private final Clock clock;

    @Override
    @Transactional
    public void processParkingCreatedEvent(ParkingCreatedEvent parkingCreatedEvent) {
        Optional<Payment> paymentOpt = paymentRepository.findByParkingId(parkingCreatedEvent.getParkingId());
        if (paymentOpt.isPresent()) {
            log.info("A payment was already created for parking with id: {}. Current payment status is: {}",
                    parkingCreatedEvent.getParkingId(),
                    paymentOpt.get().getPaymentStatus());
            return;
        }

        ApplePayCommand applePayCommand = ApplePayCommand.builder()
                .parkingId(parkingCreatedEvent.getParkingId())
                .amount(parkingCreatedEvent.getStartingFee())
                .customerId(parkingCreatedEvent.getCustomerId())
                .transactionType(TransactionType.DEBIT)
                .build();
        ApplePayCommandResponse applePayCommandResponse = applePayClient.executeTransaction(applePayCommand);

        Instant now = Instant.now(clock);
        CreatePaymentDetails createPaymentDetails = paymentDomainDataFactory
                .makeCreatePaymentDetails(parkingCreatedEvent, applePayCommandResponse, now, now);
        ResultWithDomainEvent<Payment, PaymentDebitEvent> resultWithDomainEvent = paymentDomainService
                .createPayment(createPaymentDetails);
        Payment savedPayment = paymentRepository.save(resultWithDomainEvent.getResult());
        paymentOutboxHelper.persistOutboxMessageFromEvent(resultWithDomainEvent.getEvent());
        log.info("Payment saved with id: {} and parking id: {} and status: {}",
                savedPayment.getId(),
                savedPayment.getParkingId(),
                savedPayment.getPaymentStatus());
    }

    @Override
    @Transactional
    public void processParkingStoppedEvent(ParkingStoppedEvent parkingStoppedEvent) {
        Payment payment = findPaymentByParkingId(parkingStoppedEvent.getParkingId());
        if (payment.isFeeRefundProcessDone()) {
            log.info("Parking fee difference is already refunded to customer with id: {} and parking id : {}",
                    parkingStoppedEvent.getCustomerId(),
                    parkingStoppedEvent.getParkingId());
            return;
        }

        Money refundAmount = paymentDomainService.validateForRefundAndCalculateRefundFee(payment, parkingStoppedEvent.getClosingFee());
        boolean eligibleForRefund = refundAmount.isGreaterThan(Money.ZERO);
        if (eligibleForRefund) {
            ApplePayCommandResponse applePayCommandResponse = refundWithProvider(payment, refundAmount);
            RefundPaymentDetails refundPaymentDetails = paymentDomainDataFactory
                    .makeRefundPaymentDetails(applePayCommandResponse, payment, refundAmount, Instant.now(clock));
            ResultWithDomainEvent<Payment, PaymentRefundEvent> resultWithDomainEvent = paymentDomainService.refund(refundPaymentDetails);
            paymentOutboxHelper.persistOutboxMessageFromEvent(resultWithDomainEvent.getEvent());
        } else {
            paymentDomainService.refundParkingNoop(payment);
            // No event is persisted here, since parking is already finalized STOPPED,
            // it does not wait for refund if there is nothing to refund
        }

        paymentRepository.save(payment);
        log.info("Payment updated with id: {} and parking id: {} and status: {}",
                payment.getId(),
                payment.getParkingId(),
                payment.getPaymentStatus());
    }

    private ApplePayCommandResponse refundWithProvider(Payment payment, Money refundAmount) {
        ApplePayCommand applePayCommand = ApplePayCommand.builder()
                .parkingId(payment.getParkingId())
                .amount(refundAmount)
                .customerId(payment.getCustomerId())
                .transactionType(TransactionType.CREDIT)
                .build();
        return applePayClient.executeTransaction(applePayCommand);
    }

    private Payment findPaymentByParkingId(UUID parkingId) {
        Optional<Payment> paymentOpt = paymentRepository.findByParkingId(parkingId);
        if (paymentOpt.isEmpty()) {
            throw new PaymentNotFoundException("Payment not found with parkingId: %s".formatted(parkingId));
        }
        return paymentOpt.get();
    }
}
