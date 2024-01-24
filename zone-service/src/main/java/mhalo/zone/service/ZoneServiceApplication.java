package mhalo.zone.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "mhalo")
public class ZoneServiceApplication {
    public static void main(String[] args) {
      SpringApplication.run(ZoneServiceApplication.class, args);
    }
}
