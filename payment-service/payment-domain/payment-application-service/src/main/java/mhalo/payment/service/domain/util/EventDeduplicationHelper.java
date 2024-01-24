package mhalo.payment.service.domain.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.payment.service.domain.exception.DuplicateEventException;
import mhalo.payment.service.domain.model.deduplication.ProcessedEvent;
import mhalo.payment.service.domain.ports.output.repository.ProcessedEvenRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventDeduplicationHelper {
    private final ProcessedEvenRepository processedEvenRepository;

    @Transactional
    public void deduplicateEvent(ProcessedEvent processedEvent) {
        try {
            processedEvenRepository.saveAndFlush(processedEvent);
            log.info("Event with id: {} is persisted in Processed Event table", processedEvent.getEventId());
        } catch (DataIntegrityViolationException e) {
            log.warn("Event with id: {} is already processed", processedEvent.getEventId());
            throw new DuplicateEventException(processedEvent.getEventId());
        }
    }
}
