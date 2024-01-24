package mhalo.parking.service.domain.outbox.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.domain.util.JsonUtility;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.domain.mapper.ParkingDataMapper;
import mhalo.parking.service.domain.model.event.ParkingApprovedEvent;
import mhalo.parking.service.domain.model.event.ParkingCreatedEvent;
import mhalo.parking.service.domain.model.event.ParkingEventType;
import mhalo.parking.service.domain.model.event.ParkingStoppedEvent;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;
import mhalo.parking.service.domain.ports.output.repository.ParkingOutboxRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParkingEventOutboxHelper {
    private final JsonUtility jsonUtility;
    private final ParkingDataMapper parkingDataMapper;
    private final ParkingOutboxRepository parkingOutboxRepository;
    private final Clock clock;

    @Transactional
    public void persistOutboxMessageFromEvent(ParkingCreatedEvent parkingCreatedEvent) {
        String payload = jsonUtility
                .writeValueAsString(parkingDataMapper.mapParkingCreatedEventToParkingCreatedEventOutboxPayload(parkingCreatedEvent));
        ParkingEventOutboxMessage outboxMessage = createCommonBuilder(
                parkingCreatedEvent.getParking().getCustomerId(),
                parkingCreatedEvent.getParking().getId(),
                payload,
                ParkingEventType.CREATED).build();
        parkingOutboxRepository.save(outboxMessage);
    }

    @Transactional
    public void persistOutboxMessageFromEvent(ParkingStoppedEvent parkingStoppedEvent) {
        String payload = jsonUtility
                .writeValueAsString(parkingDataMapper.mapParkingStoppedEventToParkingStoppedEventOutboxPayload(parkingStoppedEvent));
        ParkingEventOutboxMessage outboxMessage = createCommonBuilder(
                parkingStoppedEvent.getParking().getCustomerId(),
                parkingStoppedEvent.getParking().getId(),
                payload,
                ParkingEventType.STOPPED).build();
        parkingOutboxRepository.save(outboxMessage);
    }

    @Transactional
    public void persistOutboxMessageFromEvent(
            ParkingApprovedEvent parkingApprovedEvent) {
        String payload = jsonUtility
                .writeValueAsString(parkingDataMapper.mapParkingApprovedEventToParkingApprovedEventOutboxPayload(parkingApprovedEvent));
        ParkingEventOutboxMessage outboxMessage = createCommonBuilder(
                parkingApprovedEvent.getCustomerId(),
                parkingApprovedEvent.getParkingId(),
                payload,
                ParkingEventType.APPROVED).build();
        parkingOutboxRepository.save(outboxMessage);
    }

    @Transactional
    public void updateOutboxStatus(ParkingEventOutboxMessage parkingEventOutboxMessage, OutboxStatus outboxStatus) {
        parkingEventOutboxMessage.setOutboxStatus(outboxStatus);
        parkingEventOutboxMessage.setProcessedAt(Instant.now(clock));
        parkingOutboxRepository.save(parkingEventOutboxMessage);
        log.info("ParkingEventOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }

    private ParkingEventOutboxMessage.ParkingEventOutboxMessageBuilder createCommonBuilder(UUID customerId,
                                                                                           UUID parkingId,
                                                                                           String payload,
                                                                                           ParkingEventType parkingEventType) {
        return ParkingEventOutboxMessage.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .parkingId(parkingId)
                .payload(payload)
                .createdAt(Instant.now(clock))
                .parkingEventType(parkingEventType)
                .outboxStatus(OutboxStatus.STARTED);
    }
}
