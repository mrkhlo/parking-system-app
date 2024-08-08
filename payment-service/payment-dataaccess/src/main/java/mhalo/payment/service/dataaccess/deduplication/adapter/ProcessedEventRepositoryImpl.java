package mhalo.payment.service.dataaccess.deduplication.adapter;

import lombok.RequiredArgsConstructor;
import mhalo.payment.service.dataaccess.deduplication.entity.ProcessedEventEntity;
import mhalo.payment.service.dataaccess.deduplication.repository.ProcessedEventJpaRepository;
import mhalo.payment.service.domain.model.deduplication.ProcessedEvent;
import mhalo.payment.service.domain.ports.output.repository.ProcessedEvenRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessedEventRepositoryImpl implements ProcessedEvenRepository {

    private final ProcessedEventJpaRepository processedEventJpaRepository;

    @Override
    public void saveAndFlush(ProcessedEvent processedEvent) {
        ProcessedEventEntity processedEventEntity = ProcessedEventEntity.builder()
                .eventId(processedEvent.getEventId())
                .build();
        processedEventJpaRepository.saveAndFlush(processedEventEntity);
    }
}
