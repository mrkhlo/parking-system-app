package mhalo.kafka.consumer;

import org.apache.avro.specific.SpecificRecordBase;

import java.util.List;

public interface KafkaBatchConsumer<T extends SpecificRecordBase> {
    void receive(List<T> messages, List<String> keys, List<Integer> partitions, List<Long> offsets);
}
