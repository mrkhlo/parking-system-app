package mhalo.parking.service.httpclient.zone.feign;


import mhalo.httpclient.zone.FindZoneByIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "zones", url = "${parking-service.zone-service-location}")
public interface ZoneFeignClient {

    @GetMapping("/zones/{zoneId}")
    FindZoneByIdResponse getZoneById(@PathVariable UUID zoneId);
}
