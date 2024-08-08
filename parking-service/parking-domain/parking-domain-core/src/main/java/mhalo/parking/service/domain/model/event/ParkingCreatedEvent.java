package mhalo.parking.service.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
