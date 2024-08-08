package mhalo.payment.service.dataaccess.deduplication.adapter;

import mhalo.payment.service.dataaccess.deduplication.entity.ProcessedEventEntity;
import mhalo.payment.service.dataaccess.deduplication.repository.ProcessedEventJpaRepository;
import mhalo.payment.service.dataaccess.test.it.config.PaymentServiceConfiguration;
import mhalo.payment.service.domain.model.deduplication.ProcessedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@ActiveProfiles({"test", "integration-test"})
@SpringBootTest(classes = PaymentServiceConfiguration.class)
class ProcessedEventRepositoryIT {

    @Autowired
    private ProcessedEventJpaRepository processedEventJpaRepository;

    @Autowired
    private ProcessedEventRepositoryImpl processedEventRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        int port = postgres.getMappedPort(5432);
        String jdbcUrl = "jdbc:postgresql://localhost:%s/postgres?currentSchema=payment&stringtype=unspecified"
                .formatted(port);
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void should_SaveAndFlushProcessedEvent() {
        //given
        ProcessedEvent processedEvent = new ProcessedEvent(UUID.randomUUID());
        //when
        processedEventRepository.saveAndFlush(processedEvent);
        //then
        List<ProcessedEventEntity> actualEntries = processedEventJpaRepository.findAll();
        assertEquals(1, actualEntries.size());
        assertEquals(processedEvent.getEventId(), actualEntries.get(0).getEventId());
    }
}
