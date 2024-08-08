package mhalo.payment.service.dataaccess.deduplication.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_events")
@Entity
public class ProcessedEventEntity implements Persistable<UUID> {

    @Id
    private UUID eventId;
    @CreationTimestamp
    private Instant createdAt;

    @Override
    @Transient
    public UUID getId() {
        return eventId;
    }

    // Ensure Hibernate to do an INSERT with save().
    @Override
    @Transient
    public boolean isNew() {
        return true;
    }
}
