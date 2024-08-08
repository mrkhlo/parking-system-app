package mhalo.payment.service.domain.model.deduplication;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProcessedEvent {
    private final UUID eventId;
}
