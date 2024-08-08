package mhalo.payment.service.dataaccess.outbox.repository;

import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.dataaccess.outbox.entity.PaymentEventOutboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentEventOutboxMessageEntity, UUID> {
    List<PaymentEventOutboxMessageEntity> findPaymentOutboxEntitiesByOutboxStatus(OutboxStatus outboxStatus);
}
