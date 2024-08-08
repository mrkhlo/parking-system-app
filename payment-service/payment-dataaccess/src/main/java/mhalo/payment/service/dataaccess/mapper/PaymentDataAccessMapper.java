package mhalo.payment.service.dataaccess.mapper;

import mhalo.domain.model.event.model.Money;
import mhalo.payment.service.dataaccess.entity.PaymentEntity;
import mhalo.payment.service.dataaccess.entity.TransactionEntity;
import mhalo.payment.service.domain.model.Payment;
import mhalo.payment.service.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentDataAccessMapper {

    public PaymentEntity mapPaymentToPaymentEntity(Payment payment) {
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .id(payment.getId())
                .customerId(payment.getCustomerId())
                .parkingId(payment.getParkingId())
                .transactions(mapTransactionsToTransactionEntities(payment.getTransactions()))
                .paymentStatus(payment.getPaymentStatus())
                .createdAt(payment.getCreatedAt())
                .build();
        paymentEntity.getTransactions().forEach(transaction -> transaction.setPayment(paymentEntity));
        return paymentEntity;
    }

    public TransactionEntity mapTransactionToTransactionEntity(Transaction transaction) {
        return TransactionEntity.builder()
                .id(transaction.getId())
                .providerTransactionId(transaction.getProviderTransactionId())
                .amount(transaction.getAmount().getAmount())
                .transactionStatus(transaction.getTransactionStatus())
                .transactionType(transaction.getTransactionType())
                .executedAt(transaction.getExecutedAt())
                .build();
    }

    public Payment mapPaymentEntityToPayment(PaymentEntity paymentEntity) {
        return Payment.builder()
                .id(paymentEntity.getId())
                .parkingId(paymentEntity.getParkingId())
                .customerId(paymentEntity.getCustomerId())
                .createdAt(paymentEntity.getCreatedAt())
                .paymentStatus(paymentEntity.getPaymentStatus())
                .transactions(mapTransactionEntitiesToTransactions(paymentEntity.getTransactions()))
                .build();
    }

    private List<Transaction> mapTransactionEntitiesToTransactions(List<TransactionEntity> transactionEntities) {
        return transactionEntities.stream()
                .map(this::mapTransactionEntityToTransaction)
                .collect(Collectors.toList());
    }

    private Transaction mapTransactionEntityToTransaction(TransactionEntity transactionEntity) {
        return Transaction.builder()
                .id(transactionEntity.getId())
                .providerTransactionId(transactionEntity.getProviderTransactionId())
                .amount(new Money(transactionEntity.getAmount()))
                .transactionType(transactionEntity.getTransactionType())
                .transactionStatus(transactionEntity.getTransactionStatus())
                .executedAt(transactionEntity.getExecutedAt())
                .build();
    }

    private List<TransactionEntity> mapTransactionsToTransactionEntities(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::mapTransactionToTransactionEntity)
                .collect(Collectors.toList());
    }

}

