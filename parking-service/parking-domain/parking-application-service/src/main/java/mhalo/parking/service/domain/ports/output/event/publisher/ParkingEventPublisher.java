package mhalo.parking.service.domain.ports.output.event.publisher;

import mhalo.domain.ports.output.event.publisher.EventPublisher;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;

public interface ParkingEventPublisher extends EventPublisher<ParkingEventOutboxMessage, OutboxStatus> {
}
