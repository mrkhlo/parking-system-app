package mhalo.parking.service.messaging.publisher.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.domain.util.JsonUtility;
import mhalo.kafka.producer.KafkaProducerHelper;
import mhalo.kafka.producer.service.KafkaProducer;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.core.domain.ParkingStoppedEventAvroModel;
import mhalo.parking.service.domain.config.ParkingServiceConfigData;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;
import mhalo.parking.service.domain.outbox.model.ParkingStoppedEventOutboxPayload;
import mhalo.parking.service.domain.ports.output.event.publisher.ParkingStoppedEventPublisher;
import mhalo.parking.service.messaging.mapper.ParkingMessagingDataMapper;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingStoppedEventKafkaPublisher implements ParkingStoppedEventPublisher {

    private final ParkingMessagingDataMapper parkingMessagingDataMapper;
    private final KafkaProducer<String, ParkingStoppedEventAvroModel> kafkaProducer;
    private final ParkingServiceConfigData parkingServiceConfigData;
    private final JsonUtility jsonUtility;
    private final KafkaProducerHelper kafkaProducerHelper;


    @Override
    public void publish(ParkingEventOutboxMessage parkingEventOutboxMessage,
                        BiConsumer<ParkingEventOutboxMessage, OutboxStatus> outboxCallback) {
        ParkingStoppedEventOutboxPayload parkingStoppedEvent = jsonUtility
                .readValue(parkingEventOutboxMessage.getPayload(), ParkingStoppedEventOutboxPayload.class);

        ParkingStoppedEventAvroModel parkingStoppedEventAvroModel = parkingMessagingDataMapper
                .mapParkingStoppedEventToParkingStoppedEventAvroModel(parkingStoppedEvent);

        String partitionKey = parkingStoppedEvent.getParkingId().toString();
        try {
            kafkaProducer.send(parkingServiceConfigData.getParkingStoppedEventTopicName(),
                    partitionKey,
                    parkingStoppedEventAvroModel,
                    parkingEventOutboxMessage.getId(),
                    kafkaProducerHelper.getKafkaCallback(parkingServiceConfigData.getParkingStoppedEventTopicName(),
                            parkingStoppedEventAvroModel,
                            parkingEventOutboxMessage,
                            outboxCallback,
                            ParkingStoppedEventAvroModel.class.getName(),
                            partitionKey));
            log.info("ParkingStoppedEvent sent to kafka for parking id: {}",
                    partitionKey);
        } catch (Exception e) {
            log.error("Error while sending ParkingStoppedEvent to kafka for parking id: {} ," +
                    " error: {}", partitionKey, e.getMessage());
        }
    }
}
