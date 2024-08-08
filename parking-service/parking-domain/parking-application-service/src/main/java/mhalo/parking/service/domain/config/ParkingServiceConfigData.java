package mhalo.parking.service.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "parking-service")
@Data
public class ParkingServiceConfigData {
    private String parkingCreatedEventTopicName;
    private String parkingStoppedEventTopicName;
    private String parkingApprovedEventTopicName;
    private String paymentRefundEventTopicName;
    private String paymentDebitEventTopicName;
    private String parkingServiceConsumerGroupId;
    private String zoneServiceLocation;
    private String customerServiceLocation;
    private int outboxSchedulerFixedRate;
    private int outboxSchedulerInitialDelay;
}
