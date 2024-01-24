package mhalo.parking.service.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"mhalo.parking.service.dataaccess"})
@EntityScan(basePackages = {"mhalo.parking.service.dataaccess"})
@EnableFeignClients(basePackages = "mhalo.parking.service.httpclient")
@SpringBootApplication(scanBasePackages = "mhalo")
public class ParkingServiceApplication {
    public static void main(String[] args) {
      SpringApplication.run(ParkingServiceApplication.class, args);
    }
}
