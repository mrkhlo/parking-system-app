package mhalo.parking.service.dataaccess.entity;

import jakarta.persistence.*;
import lombok.*;
import mhalo.parking.service.domain.model.ParkingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parkings")
@Entity
public class ParkingEntity {
    @Id
    private UUID id;
    private UUID customerId;
    private UUID zoneId;
    private UUID trackingId;
    private String licensePlateNumber;
    private BigDecimal startingFee;
    private BigDecimal closingFee;
    private Instant startedAt;
    private Instant stoppedAt;

    @Enumerated(EnumType.STRING)
    private ParkingStatus parkingStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingEntity that = (ParkingEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
