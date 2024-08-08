package mhalo.payment.service.dataaccess.test.it.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "mhalo.payment.service.dataaccess")
@EntityScan(basePackages = "mhalo.payment.service.dataaccess")
@SpringBootApplication(scanBasePackages = {"mhalo.payment.service.dataaccess"})
public class PaymentServiceConfiguration {
}
