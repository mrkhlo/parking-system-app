package mhalo.zone.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.httpclient.zone.FindZoneByIdResponse;
import mhalo.httpclient.zone.IntervalDto;
import mhalo.httpclient.zone.RateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/zones")
public class ZoneController {

    public static UUID zoneId = UUID.fromString("8d0d2651-b42b-4198-bba1-f951ac4cf638");

    @GetMapping("/{zoneId}")
    public ResponseEntity<FindZoneByIdResponse> getZoneById(@PathVariable UUID zoneId) {
        log.info("Found zone with id: {}", zoneId);
        return ResponseEntity.ok(ZoneHolder.getInstance());
    }

    private static class ZoneHolder {
        private static final FindZoneByIdResponse INSTANCE = createSingletonZone();

        public static FindZoneByIdResponse getInstance() {
            return INSTANCE;
        }

        private static FindZoneByIdResponse createSingletonZone() {
            return FindZoneByIdResponse.builder()
                    .zoneId(zoneId)
                    .rate(RateDto.builder()
                            .amount(BigDecimal.valueOf(100))
                            .duration(Duration.ofHours(1))
                            .build())
                    .payInterval(new IntervalDto(LocalTime.of(6, 0), LocalTime.of(23, 0)))
                    .build();
        }
    }
}
