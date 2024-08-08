package mhalo.parking.service.domain.ports.input.service;

import jakarta.validation.Valid;
import mhalo.parking.service.domain.dto.rest.start.StartParkingCommand;
import mhalo.parking.service.domain.dto.rest.start.StartParkingResponse;
import mhalo.parking.service.domain.dto.rest.stop.StopParkingCommand;
import mhalo.parking.service.domain.dto.rest.track.TrackParkingResponse;

import java.util.UUID;

public interface ParkingApplicationService {

    StartParkingResponse startParking(@Valid StartParkingCommand startParkingCommand);
    TrackParkingResponse trackParking(@Valid UUID trackingId);
    void stopParking(@Valid StopParkingCommand stopParkingCommand);
}
