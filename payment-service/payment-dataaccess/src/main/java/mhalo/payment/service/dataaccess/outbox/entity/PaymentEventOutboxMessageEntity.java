package mhalo.payment.service.dataaccess.outbox.entity;

import jakarta.persistence.*;
import lombok.*;
import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.domain.model.event.PaymentEventType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_outbox")
@Entity
public class PaymentEventOutboxMessageEntity {
    @Id
    private UUID id;
    private UUID paymentId;
    private UUID parkingId;
    private UUID customerId;
    private Instant createdAt;
    private Instant processedAt;
    private String payload;
    @Enumerated(EnumType.STRING)
    private PaymentEventType paymentEventType;
    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;
    @Version
    private int version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentEventOutboxMessageEntity that = (PaymentEventOutboxMessageEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
