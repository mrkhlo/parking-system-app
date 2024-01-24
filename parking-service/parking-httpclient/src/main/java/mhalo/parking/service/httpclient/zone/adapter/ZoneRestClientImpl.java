package mhalo.parking.service.httpclient.zone.adapter;

import lombok.RequiredArgsConstructor;
import mhalo.httpclient.zone.FindZoneByIdResponse;
import mhalo.parking.service.domain.model.Zone;
import mhalo.parking.service.domain.ports.output.httpclient.ZoneRestClient;
import mhalo.parking.service.httpclient.zone.feign.ZoneFeignClient;
import mhalo.parking.service.httpclient.zone.mapper.ZoneHttpClientMapper;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class ZoneRestClientImpl implements ZoneRestClient {

    private final ZoneFeignClient zoneFeignClient;
    private final ZoneHttpClientMapper zoneHttpClientMapper;

    @Override
    public boolean isZoneExistsById(UUID id) {
        return this.getZoneById(id) != null;
    }

    @Override
    public Zone getZoneById(UUID id) {
        FindZoneByIdResponse findZoneByIdResponse = zoneFeignClient.getZoneById(id);
        return zoneHttpClientMapper.mapFindZoneByIdResponseToZone(findZoneByIdResponse);
    }
}
