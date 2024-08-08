package mhalo.payment.service.domain;

import lombok.NoArgsConstructor;
import mhalo.domain.model.event.model.Money;
import mhalo.domain.model.event.model.ResultWithDomainEvent;
import mhalo.payment.service.domain.exception.InvalidPaymentStatusException;
import mhalo.payment.service.domain.model.*;
import mhalo.payment.service.domain.model.event.PaymentDebitEvent;
import mhalo.payment.service.domain.model.event.PaymentRefundEvent;
import mhalo.payment.service.domain.dto.CreatePaymentDetails;
import mhalo.payment.service.domain.dto.RefundPaymentDetails;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class PaymentDomainService {

    public ResultWithDomainEvent<Payment, PaymentDebitEvent> createPayment(CreatePaymentDetails createPaymentDetails) {
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
                .executedAt(createPaymentDetails.executedAt())
                .build();

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .parkingId(createPaymentDetails.parkingId())
                .customerId(createPaymentDetails.customerId())
                .transactions(List.of(transaction))
                .paymentStatus(paymentStatus)
                .createdAt(createPaymentDetails.createdAt())
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

    public ResultWithDomainEvent<Payment, PaymentRefundEvent> refund(RefundPaymentDetails refundPaymentDetails) {
        TransactionStatus transactionStatus = refundPaymentDetails.isAppleTransactionSuccess() ?
                TransactionStatus.SUCCESS :
                TransactionStatus.FAILURE;

        Transaction refundTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .providerTransactionId(refundPaymentDetails.providerTransactionId())
                .amount(refundPaymentDetails.refundAmount())
                .executedAt(refundPaymentDetails.transactionExecutedAt())
                .transactionStatus(transactionStatus)
                .transactionType(TransactionType.CREDIT).build();

        Payment payment = refundPaymentDetails.payment();
        payment.refundFeeNotUtilizedTime(refundTransaction);

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
            throw new InvalidPaymentStatusException(PaymentStatus.DEBITED, payment.getPaymentStatus(),
                    "Invalid payment status for refund.");
        }

        //ignore optional, since there is always a debit transaction if the payment is in DEBITED state
        Transaction debitTransaction = payment.getSuccessfulFeeChargeTransaction().get();
        return debitTransaction.getAmount().subtract(closingFee);
    }

    public void refundParkingNoop(Payment payment) {
        payment.refundFeeNoop();
    }
}
