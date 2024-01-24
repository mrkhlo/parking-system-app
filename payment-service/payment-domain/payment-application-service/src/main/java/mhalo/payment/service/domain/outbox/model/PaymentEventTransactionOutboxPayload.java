package mhalo.payment.service.domain.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import mhalo.payment.service.domain.model.TransactionStatus;
import mhalo.payment.service.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class PaymentEventTransactionOutboxPayload {
    private UUID transactionId;
    private UUID providerTransactionId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private Instant executedAt;
}
