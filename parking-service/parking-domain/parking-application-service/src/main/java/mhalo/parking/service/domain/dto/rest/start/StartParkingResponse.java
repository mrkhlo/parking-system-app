package mhalo.parking.service.domain.dto.rest.start;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class StartParkingResponse {
    private final UUID parkingTrackingId;
    private final UUID parkingId;
}
