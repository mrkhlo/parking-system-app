package mhalo.payment.service.dataaccess.outbox.adapter;

import lombok.RequiredArgsConstructor;
import mhalo.outbox.OutboxStatus;
import mhalo.payment.service.dataaccess.outbox.entity.PaymentEventOutboxMessageEntity;
import mhalo.payment.service.dataaccess.outbox.mapper.PaymentOutboxDataMapper;
import mhalo.payment.service.dataaccess.outbox.repository.PaymentOutboxJpaRepository;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;
import mhalo.payment.service.domain.ports.output.repository.PaymentOutboxRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

    private final PaymentOutboxJpaRepository paymentOutboxJpaRepository;
    private final PaymentOutboxDataMapper paymentOutboxDataMapper;

    @Override
    public PaymentEventOutboxMessage save(PaymentEventOutboxMessage paymentEventOutboxMessage) {
        PaymentEventOutboxMessageEntity paymentEventOutboxMessageEntity = paymentOutboxDataMapper
                .mapPaymentEventOutboxMessageToPaymentEventOutboxMessageEntity(paymentEventOutboxMessage);
        return paymentOutboxDataMapper
                .mapPaymentEventOutboxMessageToPaymentEventOutboxMessage(
                        paymentOutboxJpaRepository.save(paymentEventOutboxMessageEntity));
    }

    @Override
    public List<PaymentEventOutboxMessage> getPaymentEventOutboxMessagesByOutboxStatus(OutboxStatus outboxStatus) {
        return paymentOutboxJpaRepository.findPaymentOutboxEntitiesByOutboxStatus(outboxStatus).stream()
                .map(paymentOutboxDataMapper::mapPaymentEventOutboxMessageToPaymentEventOutboxMessage)
                .collect(Collectors.toList());
    }
}
