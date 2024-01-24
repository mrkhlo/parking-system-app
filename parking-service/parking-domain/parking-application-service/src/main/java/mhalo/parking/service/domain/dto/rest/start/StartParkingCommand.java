package mhalo.parking.service.domain.dto.rest.start;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class StartParkingCommand {
    @NotNull
    private final UUID customerId;
    @NotNull
    private final UUID zoneId;
    @NotNull
    private final String licensePlateNumber;
}
