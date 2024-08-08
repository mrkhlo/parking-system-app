package mhalo.payment.service.domain.dto;

import lombok.Builder;
import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.domain.model.Payment;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RefundPaymentDetails(boolean isAppleTransactionSuccess,
                                   UUID providerTransactionId,
                                   Payment payment,
                                   Money refundAmount,
                                   Instant transactionExecutedAt){
}
