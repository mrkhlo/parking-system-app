package mhalo.kafka.consumer;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;


public interface KafkaConsumer<T extends SpecificRecordBase> {
    void receive(ConsumerRecord<String, T> consumerRecord);
}
