package mhalo.httpclient.zone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;

@Getter
@Builder
@AllArgsConstructor
public class RateDto {
    private BigDecimal amount;
    private Duration duration;
}
