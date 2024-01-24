package mhalo.parking.service.domain.dto.rest.track;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import mhalo.parking.service.domain.model.ParkingStatus;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class TrackParkingResponse {
    private final UUID parkingTrackId;
    private final ParkingStatus parkingStatus;
}
