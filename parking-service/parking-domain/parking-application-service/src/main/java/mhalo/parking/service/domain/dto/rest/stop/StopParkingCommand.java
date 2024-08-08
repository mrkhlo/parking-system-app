package mhalo.parking.service.domain.dto.rest.stop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class StopParkingCommand {
    private final UUID parkingId;
}
