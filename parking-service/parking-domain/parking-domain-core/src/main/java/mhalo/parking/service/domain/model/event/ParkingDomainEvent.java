package mhalo.parking.service.domain.model.event;


import mhalo.domain.model.event.event.DomainEvent;
import mhalo.parking.service.domain.model.Parking;

public interface ParkingDomainEvent extends DomainEvent<Parking> {
    ParkingEventType getEventType();
}
