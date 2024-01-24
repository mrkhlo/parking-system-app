package mhalo.payment.service.domain.ports.output.repository;


import mhalo.payment.service.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByParkingId(UUID parkingId);
}
