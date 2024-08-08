package mhalo.payment.service.domain.dto.httpclient.apple.pay;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.domain.model.TransactionType;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ApplePayCommand {
    @NotNull
    private final UUID customerId;
    @NotNull
    private final UUID parkingId;
    @NotNull
    private final Money amount;
    @NotNull
    private final TransactionType transactionType;
}
