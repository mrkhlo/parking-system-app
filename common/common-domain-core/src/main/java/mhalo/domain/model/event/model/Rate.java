package mhalo.domain.model.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class Rate {
    private final Money amount;
    private final Duration duration;
}
