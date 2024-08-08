package mhalo.payment.service.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment-service")
@Data
public class PaymentServiceConfigData {
    private String parkingCreatedEventTopicName;
    private String parkingStoppedEventTopicName;
    private String parkingApprovedEventTopicName;
    private String paymentRefundEventTopicName;
    private String paymentDebitEventTopicName;
    private String paymentServiceConsumerGroupId;
    private int outboxSchedulerFixedRate;
    private int outboxSchedulerInitialDelay;
}
