package mhalo.payment.service.dataaccess.entity;

import jakarta.persistence.*;
import lombok.*;
import mhalo.payment.service.domain.model.TransactionStatus;
import mhalo.payment.service.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_transactions")
@Entity
public class TransactionEntity {
    @Id
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;
    private UUID providerTransactionId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;
    private Instant executedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionEntity that = (TransactionEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
