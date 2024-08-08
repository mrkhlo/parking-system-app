package mhalo.parking.service.dataaccess.outbox.adapter;

import lombok.RequiredArgsConstructor;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.dataaccess.outbox.entity.ParkingOutboxEntity;
import mhalo.parking.service.dataaccess.outbox.mapper.ParkingOutboxDataMapper;
import mhalo.parking.service.dataaccess.outbox.repository.ParkingOutboxJpaRepository;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;
import mhalo.parking.service.domain.ports.output.repository.ParkingOutboxRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class ParkingOutboxRepositoryImpl implements ParkingOutboxRepository {

    private final ParkingOutboxJpaRepository parkingOutboxJpaRepository;
    private final ParkingOutboxDataMapper parkingOutboxDataMapper;

    @Override
    public ParkingEventOutboxMessage save(ParkingEventOutboxMessage parkingEventOutboxMessage) {
        ParkingOutboxEntity parkingOutboxEntity = parkingOutboxDataMapper
                .mapParkingEventOutboxMessageToParkingOutboxEntity(parkingEventOutboxMessage);
        return parkingOutboxDataMapper
                .mapParkingOutboxEntityToParkingEventOutboxMessage(parkingOutboxJpaRepository.save(parkingOutboxEntity));
    }

    @Override
    public List<ParkingEventOutboxMessage> getParkingEventOutboxMessagesByOutboxStatus(OutboxStatus outboxStatus) {
        return parkingOutboxJpaRepository
                .findParkingOutboxEntitiesByOutboxStatus(outboxStatus).stream()
                .map(parkingOutboxDataMapper::mapParkingOutboxEntityToParkingEventOutboxMessage)
                .collect(Collectors.toList());
    }
}
