package mhalo.payment.service.domain.config;

import mhalo.payment.service.domain.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BeanConfiguration {

    @Bean
    public PaymentDomainService paymentDomainHelper() {
        return new PaymentDomainService();
    }
}
