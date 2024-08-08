package mhalo.parking.service.domain.ports.output.repository;

import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;

import java.util.List;

public interface ParkingOutboxRepository {
    ParkingEventOutboxMessage save(ParkingEventOutboxMessage parkingEventOutboxMessage);
    List<ParkingEventOutboxMessage> getParkingEventOutboxMessagesByOutboxStatus(OutboxStatus outboxStatus);
}
