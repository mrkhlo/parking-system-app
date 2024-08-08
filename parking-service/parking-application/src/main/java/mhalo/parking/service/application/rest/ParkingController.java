package mhalo.parking.service.application.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.parking.service.domain.dto.rest.start.StartParkingCommand;
import mhalo.parking.service.domain.dto.rest.start.StartParkingResponse;
import mhalo.parking.service.domain.dto.rest.stop.StopParkingCommand;
import mhalo.parking.service.domain.dto.rest.track.TrackParkingResponse;
import mhalo.parking.service.domain.ports.input.service.ParkingApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/parkings")
public class ParkingController {

    private final ParkingApplicationService parkingApplicationService;

    @PostMapping
    public ResponseEntity<StartParkingResponse> startParking(@RequestBody StartParkingCommand startParkingCommand) {
        StartParkingResponse startParkingResponse = parkingApplicationService.startParking(startParkingCommand);
        return ResponseEntity.ok(startParkingResponse);
    }

    @PostMapping("/{parkingId}/stop")
    public ResponseEntity<Void> stopParking(@PathVariable UUID parkingId) {
        StopParkingCommand stopParkingCommand = new StopParkingCommand(parkingId);
        parkingApplicationService.stopParking(stopParkingCommand);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{trackingId}/track")
    public ResponseEntity<TrackParkingResponse> trackParking(@PathVariable UUID trackingId) {
        TrackParkingResponse trackParkingResponse = parkingApplicationService.trackParking(trackingId);
        return ResponseEntity.ok(trackParkingResponse);
    }
}
