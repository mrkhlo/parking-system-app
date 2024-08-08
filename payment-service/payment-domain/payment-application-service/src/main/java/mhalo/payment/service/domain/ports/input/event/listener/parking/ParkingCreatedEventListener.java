package mhalo.payment.service.domain.ports.input.event.listener.parking;


import mhalo.domain.ports.input.event.listener.EventListener;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;

public interface ParkingCreatedEventListener extends EventListener<ParkingCreatedEvent> {
}
