package mhalo.payment.service.domain.ports.input.event.handler.parking;

import jakarta.validation.Valid;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.dto.event.ParkingStoppedEvent;

public interface PaymentEventHandler {
    void processParkingCreatedEvent(@Valid ParkingCreatedEvent parkingCreatedEvent);
    void processParkingStoppedEvent(@Valid ParkingStoppedEvent parkingStoppedEvent);
}
