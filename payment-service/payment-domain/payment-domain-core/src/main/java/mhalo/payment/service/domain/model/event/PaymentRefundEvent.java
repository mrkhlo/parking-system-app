package mhalo.payment.service.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class PaymentRefundEvent extends PaymentDomainEvent {

    @Override
    public PaymentEventType getEventType() {
        return PaymentEventType.REFUND;
    }
}
