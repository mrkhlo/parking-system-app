package mhalo.payment.service.domain.dto;

import lombok.Builder;
import mhalo.domain.model.event.model.Money;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CreatePaymentDetails(Money startingFee,
                                   UUID parkingId,
                                   UUID customerId,
                                   boolean isAppleTransactionSuccess,
                                   UUID providerTransactionId,
                                   Instant createdAt,
                                   Instant executedAt) {}
