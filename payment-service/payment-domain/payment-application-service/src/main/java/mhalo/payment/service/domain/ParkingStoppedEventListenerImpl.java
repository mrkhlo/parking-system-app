package mhalo.payment.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhalo.payment.service.domain.dto.event.ParkingStoppedEvent;
import mhalo.payment.service.domain.model.deduplication.ProcessedEvent;
import mhalo.payment.service.domain.ports.input.event.handler.parking.PaymentEventHandler;
import mhalo.payment.service.domain.ports.input.event.listener.parking.ParkingStoppedEventListener;
import mhalo.payment.service.domain.util.EventDeduplicationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingStoppedEventListenerImpl implements ParkingStoppedEventListener {

    private final PaymentEventHandler paymentEventHandler;
    private final EventDeduplicationHelper eventDeduplicationHelper;

    @Override
    @Transactional
    public void process(ParkingStoppedEvent event) {
        ProcessedEvent processedEvent = new ProcessedEvent(event.getEventId());
        eventDeduplicationHelper.deduplicateEvent(processedEvent);
        paymentEventHandler.processParkingStoppedEvent(event);
    }
}
