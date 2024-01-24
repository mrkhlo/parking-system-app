package mhalo.payment.service.domain.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class DuplicateEventException extends PaymentApplicationServiceException {
    private final UUID eventId;

    public DuplicateEventException(UUID eventId) {
        this.eventId = eventId;
    }

    public DuplicateEventException(UUID eventId, String message) {
        super(message);
        this.eventId = eventId;
    }

    public DuplicateEventException(UUID eventId, String message, Throwable cause) {
        super(message, cause);
        this.eventId = eventId;
    }
}
