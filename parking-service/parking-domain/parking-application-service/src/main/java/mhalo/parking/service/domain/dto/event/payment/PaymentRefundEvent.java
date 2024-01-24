package mhalo.parking.service.domain.dto.event.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PaymentRefundEvent {
    private final UUID parkingId;
    private final UUID paymentId;
    private final PaymentRefundStatus paymentStatus;
}
