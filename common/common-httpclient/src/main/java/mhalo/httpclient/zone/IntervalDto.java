package mhalo.httpclient.zone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class IntervalDto {
    private final LocalTime startTime;
    private final LocalTime endTime;
}
