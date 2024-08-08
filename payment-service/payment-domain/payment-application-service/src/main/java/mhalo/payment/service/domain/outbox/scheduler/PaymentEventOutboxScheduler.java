package mhalo.payment.service.domain.outbox.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.outbox.OutboxScheduler;
import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;
import mhalo.payment.service.domain.ports.output.event.publisher.payment.PaymentEventPublisher;
import mhalo.payment.service.domain.ports.output.repository.PaymentOutboxRepository;
import mhalo.payment.service.domain.util.PaymentEventPublishers;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventOutboxScheduler implements OutboxScheduler {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final PaymentEventPublishers paymentEventPublishers;
    private final PaymentOutboxHelper paymentOutboxHelper;

    @Override
    @Transactional(readOnly = true)
    @Scheduled(fixedDelayString = "${payment-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${payment-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        List<PaymentEventOutboxMessage> outboxMessages=
                paymentOutboxRepository.getPaymentEventOutboxMessagesByOutboxStatus(OutboxStatus.STARTED);
        if (!outboxMessages.isEmpty()) {
            log.info("Received {} PaymentEventOutboxMessage with ids: {}, sending to message bus!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(outboxMessage ->
                            outboxMessage.getId().toString()).collect(Collectors.joining(",")));
            outboxMessages.forEach(outboxMessage -> {
                PaymentEventPublisher publisher = paymentEventPublishers
                        .getPaymentEventPublisher(outboxMessage.getPaymentEventType());
                publisher.publish(outboxMessage, paymentOutboxHelper::updateOutboxStatus);
            });
            log.info("{} PaymentEventOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }
}
