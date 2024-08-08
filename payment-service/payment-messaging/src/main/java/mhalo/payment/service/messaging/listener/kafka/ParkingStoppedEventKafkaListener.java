package mhalo.payment.service.messaging.listener.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.kafka.consumer.KafkaConsumer;
import mhalo.kafka.consumer.util.KafkaConsumerHelper;
import mhalo.parking.service.core.domain.ParkingStoppedEventAvroModel;
import mhalo.payment.service.domain.dto.event.ParkingStoppedEvent;
import mhalo.payment.service.domain.exception.DuplicateEventException;
import mhalo.payment.service.domain.exception.InvalidPaymentStatusException;
import mhalo.payment.service.domain.exception.PaymentApplicationServiceException;
import mhalo.payment.service.domain.exception.PaymentNotFoundException;
import mhalo.payment.service.domain.ports.input.event.listener.parking.ParkingStoppedEventListener;
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
public class ParkingStoppedEventKafkaListener implements KafkaConsumer<ParkingStoppedEventAvroModel> {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final ParkingStoppedEventListener parkingStoppedEventListener;
    private final KafkaConsumerHelper kafkaConsumerHelper;

    @Override
    @KafkaListener(groupId = "${payment-service.payment-service-consumer-group-id}", topics = "${payment-service.parking-stopped-event-topic-name}")
    public void receive(ConsumerRecord<String, ParkingStoppedEventAvroModel> consumerRecord) {
        log.info("Parking stopped event received with key:{}, partition:{} and offset: {}",
                consumerRecord.key(),
                consumerRecord.partition(),
                consumerRecord.offset());

        UUID eventId = kafkaConsumerHelper.getEventIdHeader(consumerRecord);
        ParkingStoppedEventAvroModel parkingStoppedEventAvroModel = consumerRecord.value();
        try {
            log.info("Processing parking stopped event for parking id: {}",
                    parkingStoppedEventAvroModel.getParking().getParkingId());
            ParkingStoppedEvent parkingStoppedEvent = paymentMessagingDataMapper.
                    mapParkingStoppedEventAvroModelToParkingStoppedEvent(parkingStoppedEventAvroModel, eventId);
            parkingStoppedEventListener.process(parkingStoppedEvent);
        } catch (DataAccessException e) {
            SQLException sqlException = (SQLException) e.getRootCause();
            if (sqlException != null && sqlException.getSQLState() != null &&
                    PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                //NO-OP for unique constraint exception
                log.error("Caught unique constraint exception with sql state: {} " +
                                "in ParkingStoppedEventKafkaListener for parking id: {}",
                        sqlException.getSQLState(), parkingStoppedEventAvroModel.getParking().getParkingId());
            } else {
                throw new PaymentApplicationServiceException("Throwing DataAccessException in" +
                        " ParkingStoppedEventKafkaListener: " + e.getMessage(), e);
            }
        } catch (PaymentNotFoundException e) {
            //no-op
            log.error("Payment not found. ex={}", e.getMessage());
        } catch (InvalidPaymentStatusException e) {
            //no-op
            log.error("Payment is in invalid status for . actualStatus: {}, expectedStatus: {}",
                    e.getActualStatus(), e.getExpectedStatus());
        } catch (DuplicateEventException e) {
            log.warn("Event with id: {} is already processed", e.getEventId());
        }
    }
}
