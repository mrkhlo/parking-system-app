package mhalo.domain.ports.output.event.publisher;

import java.util.function.BiConsumer;

public interface EventPublisher<T, U> {
    void publish(T parkingEventOutboxMessage,
                 BiConsumer<T, U> outboxCallback);
}
