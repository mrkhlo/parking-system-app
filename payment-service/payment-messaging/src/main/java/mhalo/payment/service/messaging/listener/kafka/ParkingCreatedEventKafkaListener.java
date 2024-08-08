package mhalo.payment.service.messaging.listener.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.kafka.consumer.KafkaConsumer;
import mhalo.kafka.consumer.util.KafkaConsumerHelper;
import mhalo.parking.service.core.domain.ParkingCreatedEventAvroModel;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.exception.DuplicateEventException;
import mhalo.payment.service.domain.exception.InvalidPaymentStatusException;
import mhalo.payment.service.domain.exception.PaymentApplicationServiceException;
import mhalo.payment.service.domain.ports.input.event.listener.parking.ParkingCreatedEventListener;
import mhalo.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingCreatedEventKafkaListener implements KafkaConsumer<ParkingCreatedEventAvroModel> {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final ParkingCreatedEventListener parkingCreatedEventListener;
    private final KafkaConsumerHelper kafkaConsumerHelper;

    @Override
    @KafkaListener(groupId = "${payment-service.payment-service-consumer-group-id}", topics = "${payment-service.parking-created-event-topic-name}")
    public void receive(ConsumerRecord<String, ParkingCreatedEventAvroModel> consumerRecord) {
        log.info("Parking created event received with key:{}, partition:{} and offset: {}",
                consumerRecord.key(),
                consumerRecord.partition(),
                consumerRecord.offset());

        UUID eventId = kafkaConsumerHelper.getEventIdHeader(consumerRecord);
        ParkingCreatedEventAvroModel parkingCreatedEventAvroModel = consumerRecord.value();
        try {
            log.info("Processing parking created event for parking id: {}",
                    parkingCreatedEventAvroModel.getParking().getParkingId());
            ParkingCreatedEvent parkingCreatedEvent = paymentMessagingDataMapper.
                    mapParkingCreatedEventAvroModelToParkingCreatedEvent(parkingCreatedEventAvroModel, eventId);
            parkingCreatedEventListener.process(parkingCreatedEvent);
        } catch (DataAccessException e) {
            SQLException sqlException = (SQLException) e.getRootCause();
            if (sqlException != null && sqlException.getSQLState() != null &&
                    PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                //no-op for unique constraint exception
                log.error("Caught unique constraint exception with sql state: {} " +
                                "in ParkingCreatedEventKafkaListener for parking id: {}",
                        sqlException.getSQLState(), parkingCreatedEventAvroModel.getParking().getParkingId());
            } else {
                throw new PaymentApplicationServiceException("Throwing DataAccessException in" +
                        " ParkingCreatedEventKafkaListener: " + e.getMessage(), e);
            }
        } catch (InvalidPaymentStatusException e) {
            //no-op
            log.error("Payment is in invalid status for . actualStatus: {}, expectedStatus: {}",
                    e.getActualStatus(), e.getExpectedStatus());
        } catch (DuplicateEventException e) {
            //no-op
            log.warn("Event with id: {} is already processed", e.getEventId());
        }
    }
}
