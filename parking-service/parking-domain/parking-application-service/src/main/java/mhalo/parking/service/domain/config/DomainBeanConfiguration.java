package mhalo.parking.service.domain.config;

import mhalo.parking.service.domain.ParkingDomainService;
import mhalo.parking.service.domain.ParkingFeeCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeanConfiguration {

    @Bean
    public ParkingFeeCalculator parkingFeeCalculator() {
        return new ParkingFeeCalculator();
    }

    @Bean
    public ParkingDomainService parkingDomainService(ParkingFeeCalculator parkingFeeCalculator) {
        return new ParkingDomainService(parkingFeeCalculator);
    }
}
