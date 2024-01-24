package mhalo.parking.service.domain.model.event;

import lombok.*;
import mhalo.parking.service.domain.model.Parking;

@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class ParkingCreatedEvent implements ParkingDomainEvent {
    private Parking parking;

    @Override
    public ParkingEventType getEventType() {
        return ParkingEventType.CREATED;
    }
}
