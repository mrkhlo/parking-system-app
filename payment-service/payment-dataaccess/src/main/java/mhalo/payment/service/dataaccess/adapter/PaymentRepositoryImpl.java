package mhalo.payment.service.dataaccess.adapter;

import lombok.RequiredArgsConstructor;
import mhalo.payment.service.dataaccess.entity.PaymentEntity;
import mhalo.payment.service.dataaccess.mapper.PaymentDataAccessMapper;
import mhalo.payment.service.dataaccess.repository.PaymentJpaRepository;
import mhalo.payment.service.domain.model.Payment;
import mhalo.payment.service.domain.ports.output.repository.PaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentDataAccessMapper paymentDataAccessMapper;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity paymentEntity = paymentDataAccessMapper.mapPaymentToPaymentEntity(payment);
        return paymentDataAccessMapper.mapPaymentEntityToPayment(
                paymentJpaRepository.save(paymentEntity));
    }

    @Override
    public Optional<Payment> findByParkingId(UUID parkingId) {
        return paymentJpaRepository.findByParkingId(parkingId)
                .map(paymentDataAccessMapper::mapPaymentEntityToPayment);
    }
}
