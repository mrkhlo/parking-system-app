package mhalo.httpclient.zone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class FindZoneByIdResponse {
    private final UUID zoneId;
    private final RateDto rate;
    private final IntervalDto payInterval;
}
