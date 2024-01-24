package mhalo.parking.service.domain.exception;

public class ParkingStartedInFreeZone extends ParkingDomainException {
    public ParkingStartedInFreeZone() {
    }

    public ParkingStartedInFreeZone(String message) {
        super(message);
    }

    public ParkingStartedInFreeZone(String message, Throwable cause) {
        super(message, cause);
    }
}
