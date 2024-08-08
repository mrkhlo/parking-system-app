package mhalo.parking.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.domain.util.JsonUtility;
import mhalo.kafka.producer.KafkaProducerHelper;
import mhalo.kafka.producer.service.KafkaProducer;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.core.domain.ParkingApprovedEventAvroModel;
import mhalo.parking.service.domain.config.ParkingServiceConfigData;
import mhalo.parking.service.domain.outbox.model.ParkingApprovedEventOutboxPayload;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;
import mhalo.parking.service.domain.ports.output.event.publisher.ParkingApprovalEventPublisher;
import mhalo.parking.service.messaging.mapper.ParkingMessagingDataMapper;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingApprovedEventKafkaPublisher implements ParkingApprovalEventPublisher {

    private final ParkingMessagingDataMapper parkingMessagingDataMapper;
    private final KafkaProducer<String, ParkingApprovedEventAvroModel> kafkaProducer;
    private final ParkingServiceConfigData parkingServiceConfigData;
    private final JsonUtility jsonUtility;
    private final KafkaProducerHelper kafkaProducerHelper;

    @Override
    public void publish(ParkingEventOutboxMessage parkingEventOutboxMessage,
                        BiConsumer<ParkingEventOutboxMessage, OutboxStatus> outboxCallback) {
        ParkingApprovedEventOutboxPayload parkingApprovedEventOutboxPayload = jsonUtility
                .readValue(parkingEventOutboxMessage.getPayload(), ParkingApprovedEventOutboxPayload.class);

        ParkingApprovedEventAvroModel parkingApprovedEventAvroModel = parkingMessagingDataMapper
                .mapParkingApprovedEventOutboxPayloadToParkingApprovedEventAvroModel(parkingApprovedEventOutboxPayload);

        String partitionKey = parkingApprovedEventOutboxPayload.getParkingId().toString();
        try {
            kafkaProducer.send(parkingServiceConfigData.getParkingApprovedEventTopicName(),
                    partitionKey,
                    parkingApprovedEventAvroModel,
                    parkingEventOutboxMessage.getId(),
                    kafkaProducerHelper.getKafkaCallback(parkingServiceConfigData.getParkingApprovedEventTopicName(),
                            parkingApprovedEventAvroModel,
                            parkingEventOutboxMessage,
                            outboxCallback,
                            ParkingApprovedEventAvroModel.class.getName(),
                            partitionKey));
            log.info("ParkingApprovedEvent sent to kafka for parking id: {}",
                    partitionKey);
        } catch (Exception e) {
            log.error("Error while sending ParkingApprovedEvent to kafka for parking id: {} ," +
                    " error: {}", partitionKey, e.getMessage());
        }
    }
}
