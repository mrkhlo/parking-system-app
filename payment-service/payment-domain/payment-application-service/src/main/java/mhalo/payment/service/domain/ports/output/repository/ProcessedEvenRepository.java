package mhalo.payment.service.domain.ports.output.repository;


import mhalo.payment.service.domain.model.deduplication.ProcessedEvent;

public interface ProcessedEvenRepository {
    void saveAndFlush(ProcessedEvent processedEvent);
}
