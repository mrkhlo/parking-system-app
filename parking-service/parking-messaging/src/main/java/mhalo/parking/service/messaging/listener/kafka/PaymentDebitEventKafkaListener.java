package mhalo.parking.service.messaging.listener.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.kafka.consumer.KafkaConsumer;
import mhalo.parking.service.core.domain.PaymentDebitEventAvroModel;
import mhalo.parking.service.domain.dto.event.payment.PaymentDebitEvent;
import mhalo.parking.service.domain.exception.InvalidParkingStatusException;
import mhalo.parking.service.domain.exception.ParkingApplicationServiceException;
import mhalo.parking.service.domain.exception.ParkingNotFoundException;
import mhalo.parking.service.domain.ports.input.event.listener.payment.PaymentDebitEventListener;
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
public class PaymentDebitEventKafkaListener implements KafkaConsumer<PaymentDebitEventAvroModel> {
    
    private final ParkingMessagingDataMapper parkingMessagingDataMapper;
    private final PaymentDebitEventListener paymentDebitEventListener;

    @Override
    @KafkaListener(groupId = "${parking-service.parking-service-consumer-group-id}", topics = "${parking-service.payment-debit-event-topic-name}")
    public void receive(ConsumerRecord<String, PaymentDebitEventAvroModel> consumerRecord) {
        log.info("Payment debit event received with key:{}, partition:{} and offset: {}",
                consumerRecord.key(),
                consumerRecord.partition(),
                consumerRecord.offset());

        PaymentDebitEventAvroModel paymentDebitEventAvroModel = consumerRecord.value();
        try {
            log.info("Processing debit payment for parking id: {}", paymentDebitEventAvroModel.getPayment().getParkingId());
            PaymentDebitEvent paymentDebitEvent = parkingMessagingDataMapper
                    .mapPaymentDebitEventAvroModelToPaymentDebitEvent(paymentDebitEventAvroModel);
            paymentDebitEventListener.process(paymentDebitEvent);
        } catch (DataAccessException e) {
            SQLException sqlException = (SQLException) e.getRootCause();
            if (sqlException != null && sqlException.getSQLState() != null &&
                    PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                //NO-OP for unique constraint exception
                log.error("Caught unique constraint exception with sql state: {} " +
                                "in PaymentDebitEventKafkaListener for parking id: {}",
                        sqlException.getSQLState(), paymentDebitEventAvroModel.getPayment().getParkingId());
            } else {
                throw new ParkingApplicationServiceException("Throwing DataAccessException in" +
                        " PaymentDebitEventKafkaListener: " + e.getMessage(), e);
            }
        } catch (ParkingNotFoundException e) {
            //no-op
            log.error("No parking found for parking id: {}", paymentDebitEventAvroModel.getPayment().getParkingId());
        } catch (InvalidParkingStatusException e) {
            //no-op
            log.error("Parking is in invalid status. actualStatus: {}, expectedStatus: {}",
                    e.getActualStatus(), e.getExpectedStatus());
        }
    }
}
