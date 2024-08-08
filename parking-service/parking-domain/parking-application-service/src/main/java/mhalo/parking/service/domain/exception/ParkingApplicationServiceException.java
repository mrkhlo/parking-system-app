package mhalo.parking.service.domain.exception;



public class ParkingApplicationServiceException extends ParkingDomainException {
    public ParkingApplicationServiceException(String message) {
        super(message);
    }

    public ParkingApplicationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
