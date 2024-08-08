package mhalo.payment.service.messaging.publisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mhalo.payment.service.messaging.test.it.config.MockWrappedBeanResetTestExecutionListener;
import mhalo.payment.service.messaging.test.it.config.PaymentServiceConfiguration;
import mhalo.payment.service.messaging.test.it.config.TestBeanConfiguration;
import mhalo.kafka.config.data.KafkaConfigData;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.core.domain.PaymentDebitEventAvroModel;
import mhalo.payment.service.domain.config.PaymentServiceConfigData;
import mhalo.payment.service.domain.model.PaymentStatus;
import mhalo.payment.service.domain.model.TransactionStatus;
import mhalo.payment.service.domain.model.TransactionType;
import mhalo.payment.service.domain.model.event.PaymentEventType;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxMessage;
import mhalo.payment.service.domain.outbox.model.PaymentEventOutboxPayload;
import mhalo.payment.service.domain.outbox.model.PaymentEventTransactionOutboxPayload;
import mhalo.payment.service.messaging.listener.kafka.ParkingCreatedEventKafkaListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.awaitility.Awaitility;
import org.awaitility.pollinterval.FixedPollInterval;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Testcontainers
@ActiveProfiles({"test", "integration-test"})
@SpringBootTest(classes = {PaymentServiceConfiguration.class, TestBeanConfiguration.class})
@TestExecutionListeners(
        listeners = {
                MockWrappedBeanResetTestExecutionListener.class,
        },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Import(ParkingCreatedEventKafkaListener.class)
@ExtendWith(MockitoExtension.class)
public class PaymentDebitEventKafkaPublisherIT {

    @Autowired
    private PaymentServiceConfigData paymentServiceConfigData;

    @Autowired
    private KafkaConfigData kafkaConfigData;

    @Autowired
    private PaymentDebitEventKafkaPublisher paymentDebitEventKafkaPublisher;

    @Autowired
    private KafkaTemplate<String, PaymentDebitEventAvroModel> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, PaymentDebitEventAvroModel> consumerFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.3"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String bootstrapServers = kafka.getBootstrapServers();
        registry.add("kafka-config.bootstrap-servers", () -> bootstrapServers);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_PublishCorrectEvent() throws JsonProcessingException {
        //given
        String topicName = paymentServiceConfigData.getPaymentDebitEventTopicName();
        kafkaTemplate.setConsumerFactory(consumerFactory);
        PaymentEventOutboxMessage paymentEventOutboxMessage = createPaymentEventOutboxMessage();


        BiConsumer<PaymentEventOutboxMessage, OutboxStatus> biConsumer = Mockito.mock(BiConsumer.class);
        //when
        paymentDebitEventKafkaPublisher.publish(paymentEventOutboxMessage, biConsumer);

        //then
        Awaitility.await()
                .pollInterval(new FixedPollInterval(Duration.ofMillis(500)))
                .atMost(5, TimeUnit.SECONDS).until(() -> {
                    ConsumerRecord<String, PaymentDebitEventAvroModel> consumerRecord =
                            kafkaTemplate.receive(topicName, 0, 0, Duration.ofMillis(300));
                    return consumerRecord.key().equals(paymentEventOutboxMessage.getParkingId().toString());
                });

        verify(biConsumer).accept(any(), any());
    }

    private PaymentEventOutboxMessage createPaymentEventOutboxMessage() throws JsonProcessingException {
        UUID parkingId = UUID.randomUUID();
        String payload = objectMapper.writeValueAsString(createPaymentEventOutboxPayload(parkingId));
        return PaymentEventOutboxMessage.builder()
                .id(UUID.randomUUID())
                .parkingId(parkingId)
                .paymentEventType(PaymentEventType.DEBIT)
                .paymentId(UUID.randomUUID())
                .createdAt(Instant.now())
                .outboxStatus(OutboxStatus.STARTED)
                .customerId(UUID.randomUUID())
                .processedAt(Instant.now())
                .payload(payload)
                .build();
    }

    private PaymentEventOutboxPayload createPaymentEventOutboxPayload(UUID parkingId) {
        return PaymentEventOutboxPayload.builder()
                .paymentId(UUID.randomUUID())
                .createdAt(Instant.now())
                .paymentStatus(PaymentStatus.DEBITED)
                .customerId(UUID.randomUUID())
                .parkingId(parkingId)
                .transaction(PaymentEventTransactionOutboxPayload.builder()
                        .transactionId(UUID.randomUUID())
                        .amount(BigDecimal.ZERO)
                        .providerTransactionId(UUID.randomUUID())
                        .transactionStatus(TransactionStatus.SUCCESS)
                        .executedAt(Instant.now())
                        .transactionType(TransactionType.DEBIT)
                        .build()).build();
    }
}
