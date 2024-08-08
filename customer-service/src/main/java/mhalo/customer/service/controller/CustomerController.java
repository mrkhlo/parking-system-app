package mhalo.customer.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/customers")
public class CustomerController {

    @GetMapping("/{customerId}/exists")
    public ResponseEntity<Boolean> isCustomerExists(@PathVariable UUID customerId) {
        log.info("Found customer with id: {}", customerId);
        return ResponseEntity.ok(true);
    }
}
