package mhalo.parking.service.dataaccess.outbox.mapper;

import mhalo.parking.service.dataaccess.outbox.entity.ParkingOutboxEntity;
import mhalo.parking.service.domain.outbox.model.ParkingEventOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class ParkingOutboxDataMapper {
    public ParkingOutboxEntity mapParkingEventOutboxMessageToParkingOutboxEntity(
            ParkingEventOutboxMessage parkingEventOutboxMessage) {
        return ParkingOutboxEntity.builder()
                .id(parkingEventOutboxMessage.getId())
                .parkingId(parkingEventOutboxMessage.getParkingId())
                .customerId(parkingEventOutboxMessage.getCustomerId())
                .createdAt(parkingEventOutboxMessage.getCreatedAt())
                .processedAt(parkingEventOutboxMessage.getProcessedAt())
                .payload(parkingEventOutboxMessage.getPayload())
                .parkingEventType(parkingEventOutboxMessage.getParkingEventType())
                .outboxStatus(parkingEventOutboxMessage.getOutboxStatus())
                .version(parkingEventOutboxMessage.getVersion())
                .build();
    }

    public ParkingEventOutboxMessage mapParkingOutboxEntityToParkingEventOutboxMessage(
            ParkingOutboxEntity parkingOutboxEntity) {
        return ParkingEventOutboxMessage.builder()
                .id(parkingOutboxEntity.getId())
                .parkingId(parkingOutboxEntity.getParkingId())
                .customerId(parkingOutboxEntity.getCustomerId())
                .createdAt(parkingOutboxEntity.getCreatedAt())
                .processedAt(parkingOutboxEntity.getProcessedAt())
                .payload(parkingOutboxEntity.getPayload())
                .parkingEventType(parkingOutboxEntity.getParkingEventType())
                .outboxStatus(parkingOutboxEntity.getOutboxStatus())
                .version(parkingOutboxEntity.getVersion())
                .build();
    }
}
