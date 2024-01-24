package mhalo.parking.service.domain.exception;

import lombok.Getter;
import mhalo.parking.service.domain.model.ParkingStatus;

@Getter
public class InvalidParkingStatusException extends ParkingDomainException {
    private final ParkingStatus actualStatus;
    private final ParkingStatus expectedStatus;

    public InvalidParkingStatusException(ParkingStatus actualStatus,
                                         ParkingStatus expectedStatus,
                                         String message) {
        super(message);
        this.actualStatus = actualStatus;
        this.expectedStatus = expectedStatus;
    }

    public InvalidParkingStatusException(ParkingStatus actualStatus,
                                         ParkingStatus expectedStatus,
                                         String message,
                                         Throwable cause) {
        super(message, cause);
        this.actualStatus = actualStatus;
        this.expectedStatus = expectedStatus;
    }
}
