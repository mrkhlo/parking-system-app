package mhalo.payment.service.domain.ports.input.event.listener.parking;


import mhalo.domain.ports.input.listener.EventListener;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;

public interface ParkingCreatedEventListener extends EventListener<ParkingCreatedEvent> {
}
