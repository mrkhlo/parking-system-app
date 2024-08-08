package mhalo.parking.service.application.exception.handler;

import lombok.extern.slf4j.Slf4j;
import mhalo.application.handler.GlobalErrorResponse;
import mhalo.application.handler.GlobalExceptionHandler;
import mhalo.parking.service.domain.exception.InvalidParkingStatusException;
import mhalo.parking.service.domain.exception.ParkingApplicationServiceException;
import mhalo.parking.service.domain.exception.ParkingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class ParkingGlobalExceptionHandler extends GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {ParkingApplicationServiceException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalErrorResponse handleException(ParkingApplicationServiceException parkingDomainException) {
        log.error(parkingDomainException.getMessage(), parkingDomainException);
        return GlobalErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(parkingDomainException.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {ParkingNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public GlobalErrorResponse handleException(ParkingNotFoundException parkingNotFoundException) {
        log.error(parkingNotFoundException.getMessage(), parkingNotFoundException);
        return GlobalErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(parkingNotFoundException.getMessage())
                .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {InvalidParkingStatusException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalErrorResponse handleException(InvalidParkingStatusException invalidParkingStatusException) {
        log.error(invalidParkingStatusException.getMessage(), invalidParkingStatusException);
        return GlobalErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(invalidParkingStatusException.getMessage())
                .build();
    }
}
