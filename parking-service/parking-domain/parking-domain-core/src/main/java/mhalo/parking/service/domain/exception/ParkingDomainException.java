package mhalo.parking.service.domain.exception;

import mhalo.domain.model.event.exception.DomainException;

public class ParkingDomainException extends DomainException {
    public ParkingDomainException() {
    }

    public ParkingDomainException(String message) {
        super(message);
    }

    public ParkingDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
