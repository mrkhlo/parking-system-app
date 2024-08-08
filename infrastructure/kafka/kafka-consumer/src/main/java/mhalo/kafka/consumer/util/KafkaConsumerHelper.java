package mhalo.kafka.consumer.util;

import mhalo.kafka.constants.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class KafkaConsumerHelper {

    public UUID getEventIdHeader(ConsumerRecord<?,?> consumerRecord) {
        String eventId = new String(consumerRecord.headers()
                .headers(KafkaConstants.HEADER_EVENT_ID)
                .iterator().next().value(), StandardCharsets.UTF_8);
        return UUID.fromString(eventId);
    }
}
