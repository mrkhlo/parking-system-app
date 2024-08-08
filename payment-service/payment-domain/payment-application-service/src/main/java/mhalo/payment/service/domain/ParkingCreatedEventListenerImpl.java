package mhalo.payment.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.payment.service.domain.dto.event.ParkingCreatedEvent;
import mhalo.payment.service.domain.model.deduplication.ProcessedEvent;
import mhalo.payment.service.domain.ports.input.event.handler.parking.PaymentEventHandler;
import mhalo.payment.service.domain.ports.input.event.listener.parking.ParkingCreatedEventListener;
import mhalo.payment.service.domain.util.EventDeduplicationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingCreatedEventListenerImpl implements ParkingCreatedEventListener {

    private final PaymentEventHandler paymentEventHandler;
    private final EventDeduplicationHelper eventDeduplicationHelper;

    @Override
    @Transactional
    public void process(ParkingCreatedEvent event) {
        ProcessedEvent processedEvent = new ProcessedEvent(event.getEventId());
        eventDeduplicationHelper.deduplicateEvent(processedEvent);
        paymentEventHandler.processParkingCreatedEvent(event);
    }
}
