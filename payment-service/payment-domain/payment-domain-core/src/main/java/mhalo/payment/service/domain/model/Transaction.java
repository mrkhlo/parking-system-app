package mhalo.payment.service.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mhalo.domain.model.event.model.BaseEntity;
import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.domain.exception.PaymentDomainException;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Transaction extends BaseEntity<UUID> {
    private final UUID providerTransactionId;
    private final Money amount;
    private final TransactionType transactionType;
    private final TransactionStatus transactionStatus;
    private final Instant executedAt;

    void validateInvariants() {
        if (getId() == null || providerTransactionId == null || amount == null ||
                transactionType == null || transactionStatus == null || executedAt == null) {
            throw new PaymentDomainException("Transaction invariants are not met for transaction: %s".formatted(getId().toString()));
        }
    }

    private Transaction(Builder builder) {
        setId(builder.id);
        providerTransactionId = builder.providerTransactionId;
        amount = builder.amount;
        transactionType = builder.transactionType;
        transactionStatus = builder.transactionStatus;
        executedAt = builder.executedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID id;
        private UUID providerTransactionId;
        private Money amount;
        private TransactionType transactionType;
        private TransactionStatus transactionStatus;
        private Instant executedAt;

        private Builder() {
        }

        public Builder id(UUID val) {
            id = val;
            return this;
        }

        public Builder providerTransactionId(UUID val) {
            providerTransactionId = val;
            return this;
        }

        public Builder amount(Money val) {
            amount = val;
            return this;
        }

        public Builder transactionType(TransactionType val) {
            transactionType = val;
            return this;
        }

        public Builder transactionStatus(TransactionStatus val) {
            transactionStatus = val;
            return this;
        }

        public Builder executedAt(Instant val) {
            executedAt = val;
            return this;
        }

        public Transaction build() {
            Transaction transaction = new Transaction(this);
            transaction.validateInvariants();
            return transaction;
        }
    }
}
