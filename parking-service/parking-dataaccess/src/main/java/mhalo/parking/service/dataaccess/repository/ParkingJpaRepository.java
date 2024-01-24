package mhalo.parking.service.dataaccess.repository;

import mhalo.parking.service.dataaccess.entity.ParkingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParkingJpaRepository extends JpaRepository<ParkingEntity, UUID> {
    Optional<ParkingEntity> findByTrackingId(UUID trackingId);
}
