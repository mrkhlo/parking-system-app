package mhalo.payment.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.domain.util.JsonUtility;
import mhalo.kafka.producer.KafkaProducerHelper;
import mhalo.kafka.producer.service.KafkaProducer;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.core.domain.PaymentDebitEventAvroModel;
import mhalo.payment.service.domain.config.PaymentServiceConfigData;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxPayload;
import mhalo.payment.service.domain.ports.output.event.publisher.payment.PaymentDebitEventPublisher;
import mhalo.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDebitEventKafkaPublisher implements PaymentDebitEventPublisher {
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final KafkaProducer<String, PaymentDebitEventAvroModel> kafkaProducer;
    private final PaymentServiceConfigData paymentServiceConfigData;
    private final JsonUtility jsonUtility;
    private final KafkaProducerHelper kafkaProducerHelper;

    @Override
    public void publish(PaymentEventOutboxMessage paymentEventOutboxMessage,
                        BiConsumer<PaymentEventOutboxMessage, OutboxStatus> outboxCallback) {
        PaymentEventOutboxPayload outboxMessagePayload = jsonUtility.readValue(
                paymentEventOutboxMessage.getPayload(), PaymentEventOutboxPayload.class);

        PaymentDebitEventAvroModel paymentDebitEventAvroModel = paymentMessagingDataMapper
                .mapPaymentEventOutboxPayloadToPaymentDebitEventAvroModel(outboxMessagePayload);

        String partitionKey = outboxMessagePayload.getParkingId().toString();
        try {
            kafkaProducer.send(paymentServiceConfigData.getPaymentDebitEventTopicName(),
                    partitionKey,
                    paymentDebitEventAvroModel,
                    paymentEventOutboxMessage.getId(),
                    kafkaProducerHelper.getKafkaCallback(paymentServiceConfigData.getPaymentDebitEventTopicName(),
                            paymentDebitEventAvroModel,
                            paymentEventOutboxMessage,
                            outboxCallback,
                            PaymentDebitEventAvroModel.class.getName(),
                            partitionKey));
            log.info("PaymentDebitEvent sent to kafka for parking id: {}", partitionKey);
        } catch (Exception e) {
            log.error("Error while sending PaymentDebitEvent to kafka for parking id: {} ," +
                    " error: {}", partitionKey, e.getMessage());
        }
    }
}
