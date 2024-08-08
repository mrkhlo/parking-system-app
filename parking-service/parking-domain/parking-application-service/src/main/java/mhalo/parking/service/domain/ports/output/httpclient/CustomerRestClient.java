package mhalo.parking.service.domain.ports.output.httpclient;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface CustomerRestClient {
    boolean isCustomerExistsById(@NotNull UUID id);
}
