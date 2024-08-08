package mhalo.payment.service.dataaccess.entity;

import jakarta.persistence.*;
import lombok.*;
import mhalo.payment.service.domain.model.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
@Entity
public class PaymentEntity {
    @Id
    private UUID id;
    private UUID parkingId;
    private UUID customerId;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<TransactionEntity> transactions;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentEntity that = (PaymentEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
