package mhalo.parking.service.domain.ports.output.httpclient;

import jakarta.validation.constraints.NotNull;
import mhalo.parking.service.domain.model.Zone;

import java.util.UUID;

public interface ZoneRestClient {
    boolean isZoneExistsById(@NotNull UUID id);
    Zone getZoneById(@NotNull UUID id);
}
