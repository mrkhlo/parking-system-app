package mhalo.parking.service.httpclient.customer.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "customers", url = "${parking-service.customer-service-location}")
public interface CustomerFeignClient {

    @GetMapping("/customers/{customerId}/exists")
    boolean isCustomerExists(@PathVariable UUID customerId);
}
