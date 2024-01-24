package mhalo.parking.service.httpclient.customer.adapter;

import lombok.RequiredArgsConstructor;
import mhalo.parking.service.domain.ports.output.httpclient.CustomerRestClient;
import mhalo.parking.service.httpclient.customer.feign.CustomerFeignClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRestClientImpl implements CustomerRestClient {

    private final CustomerFeignClient customerFeignClient;

    @Override
    public boolean isCustomerExistsById(UUID id) {
        return customerFeignClient.isCustomerExists(id);
    }
}
