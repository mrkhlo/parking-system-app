package mhalo.parking.service.messaging.listener.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.kafka.consumer.KafkaConsumer;
import mhalo.parking.service.core.domain.PaymentRefundEventAvroModel;
import mhalo.parking.service.domain.dto.event.payment.PaymentRefundEvent;
import mhalo.parking.service.domain.exception.InvalidParkingStatusException;
import mhalo.parking.service.domain.exception.ParkingApplicationServiceException;
import mhalo.parking.service.domain.exception.ParkingNotFoundException;
import mhalo.parking.service.domain.ports.input.event.listener.payment.PaymentRefundEventListener;
import mhalo.parking.service.messaging.mapper.ParkingMessagingDataMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRefundEventKafkaListener implements KafkaConsumer<PaymentRefundEventAvroModel> {
    private final ParkingMessagingDataMapper parkingMessagingDataMapper;
    private final PaymentRefundEventListener paymentRefundEventListener;

    @Override
    @KafkaListener(groupId = "${parking-service.parking-service-consumer-group-id}", topics = "${parking-service.payment-refund-event-topic-name}")
    public void receive(ConsumerRecord<String, PaymentRefundEventAvroModel> consumerRecord) {
        log.info("Payment refund event received with key:{}, partition:{} and offset: {}",
                consumerRecord.key(),
                consumerRecord.partition(),
                consumerRecord.offset());

        PaymentRefundEventAvroModel paymentRefundEventAvroModel = consumerRecord.value();
        try {
            log.info("Processing refund payment for parking id: {}", paymentRefundEventAvroModel.getPayment().getParkingId());
            PaymentRefundEvent paymentRefundEvent = parkingMessagingDataMapper
                    .mapPaymentRefundEventAvroModelToPaymentRefundEvent(paymentRefundEventAvroModel);
            paymentRefundEventListener.process(paymentRefundEvent);
        } catch (DataAccessException e) {
            SQLException sqlException = (SQLException) e.getRootCause();
            if (sqlException != null && sqlException.getSQLState() != null &&
                    PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                //NO-OP for unique constraint exception
                log.error("Caught unique constraint exception with sql state: {} " +
                                "in PaymentRefundEventKafkaListener for parking id: {}",
                        sqlException.getSQLState(), paymentRefundEventAvroModel.getPayment().getParkingId());
            } else {
                throw new ParkingApplicationServiceException("Throwing DataAccessException in" +
                        " PaymentRefundEventKafkaListener: " + e.getMessage(), e);
            }
        } catch (ParkingNotFoundException e) {
            //no-op
            log.error("No parking found for parking id: {}", paymentRefundEventAvroModel.getPayment().getParkingId());
        } catch (InvalidParkingStatusException e) {
            //no-op
            log.error("Parking is in invalid status. actualStatus: {}, expectedStatus: {}",
                    e.getActualStatus(), e.getExpectedStatus());
        }
    }
}
