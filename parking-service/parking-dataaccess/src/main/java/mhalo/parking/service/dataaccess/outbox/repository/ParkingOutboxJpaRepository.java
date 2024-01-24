package mhalo.parking.service.dataaccess.outbox.repository;

import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.dataaccess.outbox.entity.ParkingOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParkingOutboxJpaRepository extends JpaRepository<ParkingOutboxEntity, UUID> {
    List<ParkingOutboxEntity> findParkingOutboxEntitiesByOutboxStatus(OutboxStatus outboxStatus);
}
