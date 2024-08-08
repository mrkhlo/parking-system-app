package mhalo.payment.service.domain.outbox.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.domain.util.JsonUtility;
import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.domain.mapper.PaymentDataMapper;
import mhalo.payment.service.domain.model.event.PaymentDomainEvent;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;
import mhalo.payment.service.domain.ports.output.repository.PaymentOutboxRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentOutboxHelper {

    private final JsonUtility jsonUtility;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final Clock clock;

    @Transactional
    public void persistOutboxMessageFromEvent(PaymentDomainEvent paymentDomainEvent) {
        String payload = jsonUtility.writeValueAsString(paymentDataMapper
                .mapPaymentDomainEventToPaymentEventOutboxPayload(paymentDomainEvent));
        PaymentEventOutboxMessage outboxMessage = createPaymentEventOutboxMessage(paymentDomainEvent, payload);
        paymentOutboxRepository.save(outboxMessage);
        log.info("Saved outbox message");
    }

    @Transactional
    public void updateOutboxStatus(PaymentEventOutboxMessage PaymentEventOutboxMessage, OutboxStatus outboxStatus) {
        PaymentEventOutboxMessage.setOutboxStatus(outboxStatus);
        PaymentEventOutboxMessage.setProcessedAt(Instant.now(clock));
        paymentOutboxRepository.save(PaymentEventOutboxMessage);
        log.info("PaymentEventOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }

    private PaymentEventOutboxMessage createPaymentEventOutboxMessage(PaymentDomainEvent paymentDomainEvent,
                                                                      String payload) {
        return PaymentEventOutboxMessage.builder()
                .id(UUID.randomUUID())
                .paymentId(paymentDomainEvent.getPaymentId())
                .parkingId(paymentDomainEvent.getParkingId())
                .customerId(paymentDomainEvent.getCustomerId())
                .paymentEventType(paymentDomainEvent.getEventType())
                .createdAt(Instant.now(clock))
                .outboxStatus(OutboxStatus.STARTED)
                .payload(payload)
                .build();
    }
}
