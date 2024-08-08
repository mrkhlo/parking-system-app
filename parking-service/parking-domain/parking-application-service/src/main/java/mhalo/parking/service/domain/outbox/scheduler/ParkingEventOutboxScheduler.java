package mhalo.parking.service.domain.outbox.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.outbox.OutboxScheduler;
import mhalo.outbox.OutboxStatus;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;
import mhalo.parking.service.domain.ports.output.event.publisher.ParkingEventPublisher;
import mhalo.parking.service.domain.ports.output.repository.ParkingOutboxRepository;
import mhalo.parking.service.domain.util.ParkingEventPublishers;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingEventOutboxScheduler implements OutboxScheduler {

    private final ParkingOutboxRepository parkingOutboxRepository;
    private final ParkingEventPublishers parkingEventPublishers;
    private final ParkingEventOutboxHelper parkingEventOutboxHelper;

    @Override
    @Transactional(readOnly = true)
    @Scheduled(fixedDelayString = "${parking-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${parking-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        List<ParkingEventOutboxMessage> outboxMessages=
                parkingOutboxRepository.getParkingEventOutboxMessagesByOutboxStatus(OutboxStatus.STARTED);
        if (!outboxMessages.isEmpty()) {
            log.info("Received {} ParkingEventOutboxMessage with ids: {}, sending to message bus!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(outboxMessage ->
                            outboxMessage.getId().toString()).collect(Collectors.joining(",")));
            outboxMessages.forEach(outboxMessage -> {
                ParkingEventPublisher publisher = parkingEventPublishers
                        .getParkingEventPublisher(outboxMessage.getParkingEventType());
                publisher.publish(outboxMessage, parkingEventOutboxHelper::updateOutboxStatus);
            });

            log.info("{} ParkingEventOutboxMessage sent to message bus!", outboxMessages.size());

        }
    }

}
