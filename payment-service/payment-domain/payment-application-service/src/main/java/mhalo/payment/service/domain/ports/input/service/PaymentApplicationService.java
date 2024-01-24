package mhalo.payment.service.domain.ports.input.service;

import jakarta.validation.Valid;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.dto.event.ParkingStoppedEvent;

public interface PaymentApplicationService {
    void payParking(@Valid ParkingCreatedEvent parkingCreatedEvent);
    void refundParkingFeeDiff(@Valid ParkingStoppedEvent parkingStoppedEvent);
}
