package mhalo.payment.service.messaging.test.it.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/*@SpringBootApplication(scanBasePackages = {"mhalo.kafka", "mhalo.payment.service.messaging.mapper", "mhalo.payment.service.domain.config"})*/
@SpringBootApplication(scanBasePackages = {"mhalo.kafka", "mhalo.payment.service.messaging", "mhalo.payment.service.domain.config", "mhalo.domain.util"})
public class PaymentServiceConfiguration {


}
