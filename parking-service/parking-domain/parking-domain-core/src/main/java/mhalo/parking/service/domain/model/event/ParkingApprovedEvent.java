package mhalo.parking.service.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class ParkingApprovedEvent implements ParkingDomainEvent {
    private UUID parkingId;
    private UUID customerId;

    @Override
    public ParkingEventType getEventType() {
        return ParkingEventType.APPROVED;
    }
}
