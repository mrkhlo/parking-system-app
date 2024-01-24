package mhalo.parking.service.domain.util;

import lombok.RequiredArgsConstructor;
import mhalo.parking.service.domain.model.event.ParkingEventType;
import mhalo.parking.service.domain.ports.output.event.publisher.ParkingApprovalEventPublisher;
import mhalo.parking.service.domain.ports.output.event.publisher.ParkingCreatedEventPublisher;
import mhalo.parking.service.domain.ports.output.event.publisher.ParkingEventPublisher;
import mhalo.parking.service.domain.ports.output.event.publisher.ParkingStoppedEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParkingEventPublishers {

    private final ParkingCreatedEventPublisher parkingCreatedEventPublisher;
    private final ParkingApprovalEventPublisher parkingApprovedEventPublisher;
    private final ParkingStoppedEventPublisher parkingStoppedEventPublisher;

    public ParkingEventPublisher getParkingEventPublisher(ParkingEventType parkingEventType) {
        return switch (parkingEventType) {
            case APPROVED -> parkingApprovedEventPublisher;
            case CREATED -> parkingCreatedEventPublisher;
            case STOPPED -> parkingStoppedEventPublisher;
        };
    }
}
