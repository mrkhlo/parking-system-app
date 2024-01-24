package mhalo.parking.service.domain.exception;

public class ParkingNotFoundException extends ParkingDomainException {
    public ParkingNotFoundException(String message) {
        super(message);
    }

    public ParkingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
