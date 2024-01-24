package mhalo.payment.service.domain;

import lombok.NoArgsConstructor;
import mhalo.domain.model.event.model.Money;
import mhalo.domain.model.event.model.ResultWithDomainEvent;
import mhalo.payment.service.domain.paramwrapper.CreatePaymentDetails;
import mhalo.payment.service.domain.exception.InvalidPaymentStatusException;
import mhalo.payment.service.domain.model.*;
import mhalo.payment.service.domain.model.event.PaymentRefundEvent;
import mhalo.payment.service.domain.model.event.PaymentDebitEvent;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class PaymentDomainService {

    private Clock clock;
    public PaymentDomainService(Clock clock) {
        this.clock = clock;
    }

    public ResultWithDomainEvent<Payment, PaymentDebitEvent> createPayment(CreatePaymentDetails createPaymentDetails) {
        Instant now = Instant.now(clock);
        PaymentStatus paymentStatus;
        TransactionStatus transactionStatus;
        if (createPaymentDetails.isAppleTransactionSuccess()) {
            paymentStatus = PaymentStatus.DEBITED;
            transactionStatus = TransactionStatus.SUCCESS;
        } else {
            paymentStatus = PaymentStatus.DEBIT_FAILED;
            transactionStatus = TransactionStatus.FAILURE;
        }

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .providerTransactionId(createPaymentDetails.providerTransactionId())
                .amount(createPaymentDetails.startingFee())
                .transactionType(TransactionType.DEBIT)
                .transactionStatus(transactionStatus)
                .executedAt(now)
                .build();

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .parkingId(createPaymentDetails.parkingId())
                .customerId(createPaymentDetails.customerId())
                .transactions(List.of(transaction))
                .paymentStatus(paymentStatus)
                .createdAt(now)
                .build();

        PaymentDebitEvent paymentDebitEvent = PaymentDebitEvent.builder()
                .paymentId(payment.getId())
                .parkingId(payment.getParkingId())
                .customerId(payment.getCustomerId())
                .createdAt(payment.getCreatedAt())
                .paymentStatus(payment.getPaymentStatus())
                .transaction(transaction).build();

        return ResultWithDomainEvent.<Payment, PaymentDebitEvent>builder()
                .result(payment)
                .event(paymentDebitEvent)
                .build();
    }

    public ResultWithDomainEvent<Payment, PaymentRefundEvent> refund(boolean isAppleTransactionSuccess,
                                                                     UUID providerTransactionId,
                                                                     Payment payment,
                                                                     Money refundAmount) {
        TransactionStatus transactionStatus = isAppleTransactionSuccess ?
                TransactionStatus.SUCCESS :
                TransactionStatus.FAILURE;

        Transaction refundTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .providerTransactionId(providerTransactionId)
                .amount(refundAmount)
                .executedAt(Instant.now(clock))
                .transactionStatus(transactionStatus)
                .transactionType(TransactionType.CREDIT).build();

        payment.refund(refundTransaction);

        PaymentRefundEvent paymentRefundEvent = PaymentRefundEvent.builder()
                .paymentId(payment.getId())
                .parkingId(payment.getParkingId())
                .customerId(payment.getCustomerId())
                .createdAt(payment.getCreatedAt())
                .paymentStatus(payment.getPaymentStatus())
                .transaction(refundTransaction).build();

        return ResultWithDomainEvent.<Payment, PaymentRefundEvent>builder()
                .result(payment)
                .event(paymentRefundEvent)
                .build();
    }

    public Money validateForRefundAndCalculateRefundFee(Payment payment, Money closingFee) {
        if (payment.getPaymentStatus() != PaymentStatus.DEBITED) {
            throw new InvalidPaymentStatusException(payment.getPaymentStatus(), PaymentStatus.DEBITED,
                    "Invalid payment status for refund.");
        }

        Transaction debitTransaction = payment.getDebitSuccessTransaction().get();
        return debitTransaction.getAmount().subtract(closingFee);
    }

    public void refundParkingNoop(Payment payment) {
        payment.refundParkingFeeNoop();
    }
}
