package mhalo.parking.service.dataaccess.outbox.entity;

import jakarta.persistence.*;
import lombok.*;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.domain.model.event.ParkingEventType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parking_outbox")
@Entity
public class ParkingOutboxEntity {
    @Id
    private UUID id;
    private UUID parkingId;
    private UUID customerId;
    private Instant createdAt;
    private Instant processedAt;
    private String payload;
    @Enumerated(EnumType.STRING)
    private ParkingEventType parkingEventType;
    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;
    @Version
    private int version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingOutboxEntity that = (ParkingOutboxEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
