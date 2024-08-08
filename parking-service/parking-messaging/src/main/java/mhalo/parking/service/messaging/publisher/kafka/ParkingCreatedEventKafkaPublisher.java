package mhalo.parking.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.domain.util.JsonUtility;
import mhalo.kafka.producer.KafkaProducerHelper;
import mhalo.kafka.producer.service.KafkaProducer;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.core.domain.ParkingCreatedEventAvroModel;
import mhalo.parking.service.domain.config.ParkingServiceConfigData;
import mhalo.parking.service.domain.outbox.model.ParkingCreatedEventOutboxPayload;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;
import mhalo.parking.service.domain.ports.output.event.publisher.ParkingCreatedEventPublisher;
import mhalo.parking.service.messaging.mapper.ParkingMessagingDataMapper;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingCreatedEventKafkaPublisher implements ParkingCreatedEventPublisher {

    private final ParkingMessagingDataMapper parkingMessagingDataMapper;
    private final KafkaProducer<String, ParkingCreatedEventAvroModel> kafkaProducer;
    private final ParkingServiceConfigData parkingServiceConfigData;
    private final JsonUtility jsonUtility;
    private final KafkaProducerHelper kafkaProducerHelper;


    @Override
    public void publish(ParkingEventOutboxMessage parkingEventOutboxMessage,
                        BiConsumer<ParkingEventOutboxMessage, OutboxStatus> outboxCallback) {
        ParkingCreatedEventOutboxPayload parkingCreatedEvent = jsonUtility
                .readValue(parkingEventOutboxMessage.getPayload(), ParkingCreatedEventOutboxPayload.class);

        ParkingCreatedEventAvroModel parkingCreatedEventAvroModel = parkingMessagingDataMapper
                .mapParkingCreatedEventToParkingCreatedEventAvroModel(parkingCreatedEvent);

        String partitionKey = parkingCreatedEvent.getParkingId().toString();
        try {
            kafkaProducer.send(
                    parkingServiceConfigData.getParkingCreatedEventTopicName(),
                    partitionKey,
                    parkingCreatedEventAvroModel,
                    parkingEventOutboxMessage.getId(),
                    kafkaProducerHelper.getKafkaCallback(parkingServiceConfigData.getParkingCreatedEventTopicName(),
                            parkingCreatedEventAvroModel,
                            parkingEventOutboxMessage,
                            outboxCallback,
                            ParkingCreatedEventAvroModel.class.getName(),
                            partitionKey));
            log.info("ParkingCreatedEvent sent to kafka for parking id: {}",
                    partitionKey);
        } catch (Exception e) {
            log.error("Error while sending ParkingCreatedEvent to kafka for parking id: {} ," +
                    " error: {}", partitionKey, e.getMessage());
        }
    }
}
