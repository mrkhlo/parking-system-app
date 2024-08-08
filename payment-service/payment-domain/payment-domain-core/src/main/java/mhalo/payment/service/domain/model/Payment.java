package mhalo.payment.service.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mhalo.domain.model.event.model.AggregateRoot;
import mhalo.payment.service.domain.exception.InvalidPaymentStatusException;
import mhalo.payment.service.domain.exception.PaymentDomainException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Payment extends AggregateRoot<UUID> {
    private final UUID parkingId;
    private final UUID customerId;
    private final Instant createdAt;
    private List<Transaction> transactions;
    private PaymentStatus paymentStatus;

    public boolean isFeeRefundProcessDone() {
        return paymentStatus == PaymentStatus.REFUNDED ||
                paymentStatus == PaymentStatus.REFUND_FAILED ||
                paymentStatus == PaymentStatus.REFUND_NOOP;
    }

    public boolean isFeeChargeProcessDone() {
        return paymentStatus == PaymentStatus.DEBITED || paymentStatus == PaymentStatus.DEBIT_FAILED;
    }

    public Optional<Transaction> getSuccessfulFeeChargeTransaction() {
        return transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEBIT &&
                        t.getTransactionStatus() == TransactionStatus.SUCCESS)
                .findAny();
    }

    public void refundFeeNotUtilizedTime(Transaction refundTransaction) {
        if (paymentStatus != PaymentStatus.DEBITED) {
            throw new InvalidPaymentStatusException(PaymentStatus.DEBITED, paymentStatus,
                    "Invalid payment status for refund.");
        }

        if (refundTransaction.getTransactionType() != TransactionType.CREDIT) {
            throw new PaymentDomainException("Cannot refund with DEBIT transaction");
        }

        transactions.add(refundTransaction);
        paymentStatus = refundTransaction.getTransactionStatus() == TransactionStatus.SUCCESS ?
                PaymentStatus.REFUNDED :
                PaymentStatus.REFUND_FAILED;
    }

    public void refundFeeNoop() {
        if (paymentStatus != PaymentStatus.DEBITED) {
            throw new InvalidPaymentStatusException(PaymentStatus.DEBITED, paymentStatus,
                    "Invalid payment status for refund.");
        }
        paymentStatus = PaymentStatus.REFUND_NOOP;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    private void validateFieldsNotNull(PaymentStatus paymentStatus) {
        if (paymentStatus == null || getId() == null || parkingId == null || customerId == null ||
                createdAt == null || transactions == null || transactions.isEmpty()) {
            throw new PaymentDomainException("Payment invariants are not met for for status: " + paymentStatus);
        }
    }

    private void validateInvariants() {
        validateFieldsNotNull(paymentStatus);
        switch (paymentStatus) {
            case DEBITED, DEBIT_FAILED -> validateDebitStatus(paymentStatus);
            case REFUNDED, REFUND_FAILED -> validateRefundStatus(paymentStatus);
            case REFUND_NOOP -> validateRefundNoopStatus(paymentStatus);
        }
    }

    private void validateTransactionExists(PaymentStatus paymentStatus, TransactionType transactionType, TransactionStatus transactionStatus) {
        transactions.stream().filter(t -> t.getTransactionStatus() == transactionStatus &&
                        t.getTransactionType() == transactionType).findAny()
                .orElseThrow(() -> new PaymentDomainException(("Payment invariants are not met for for status: %s. " +
                        "%s transaction not found with status: %s.").formatted(paymentStatus, transactionType.name(), transactionStatus)));
    }

    private void validateRefundStatus(PaymentStatus refundStatus) {
        validateTransactionExists(refundStatus, TransactionType.DEBIT, TransactionStatus.SUCCESS);

        TransactionStatus expectedRefundTransactionStatus = refundStatus == PaymentStatus.REFUNDED ?
                TransactionStatus.SUCCESS :
                TransactionStatus.FAILURE;
        validateTransactionExists(refundStatus, TransactionType.CREDIT, expectedRefundTransactionStatus);
    }

    private void validateRefundNoopStatus(PaymentStatus refundStatus) {
        validateTransactionExists(refundStatus, TransactionType.DEBIT, TransactionStatus.SUCCESS);
    }

    private void validateDebitStatus(PaymentStatus debitStatus) {
        TransactionStatus expectedDebitTransactionStatus = debitStatus == PaymentStatus.DEBITED ?
                TransactionStatus.SUCCESS :
                TransactionStatus.FAILURE;
        validateTransactionExists(debitStatus, TransactionType.DEBIT, expectedDebitTransactionStatus);
    }

    /**
     * Builder which supposed to be used only for creating payment with its initial state,
     * rehydrating payment from the database and creating test data.
     * Since this way, multiple state changes, hence invariant checks can be "skipped",
     * validating all invariants here is a must.
     */
    private Payment(Builder builder) {
        setId(builder.id);
        parkingId = builder.parkingId;
        customerId = builder.customerId;
        createdAt = builder.createdAt;
        transactions = new ArrayList<>(builder.transactions);
        paymentStatus = builder.paymentStatus;
    }

    public static Payment.Builder builder() {
        return new Payment.Builder();
    }

    public static final class Builder {
        private UUID id;
        private UUID parkingId;
        private UUID customerId;
        private Instant createdAt;
        private List<Transaction> transactions;
        private PaymentStatus paymentStatus;

        private Builder() {
        }

        public Builder id(UUID val) {
            id = val;
            return this;
        }

        public Builder parkingId(UUID val) {
            parkingId = val;
            return this;
        }

        public Builder customerId(UUID val) {
            customerId = val;
            return this;
        }

        public Builder createdAt(Instant val) {
            createdAt = val;
            return this;
        }

        public Builder transactions(List<Transaction> val) {
            transactions = new ArrayList<>(val);
            return this;
        }

        public Builder paymentStatus(PaymentStatus val) {
            paymentStatus = val;
            return this;
        }

        public Payment build() {
            Payment payment = new Payment(this);
            payment.validateInvariants();
            return payment;
        }
    }
}
