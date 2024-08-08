package mhalo.parking.service.domain.ports.output.repository;


import mhalo.parking.service.domain.model.Parking;

import java.util.Optional;
import java.util.UUID;

public interface ParkingRepository {
    Parking save(Parking parking);
    Optional<Parking> findById(UUID id);
    Optional<Parking> findByTrackingId(UUID trackingId);
}
