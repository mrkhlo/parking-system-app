package mhalo.payment.service.dataaccess.deduplication.repository;

import mhalo.payment.service.dataaccess.deduplication.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, UUID> {
}
