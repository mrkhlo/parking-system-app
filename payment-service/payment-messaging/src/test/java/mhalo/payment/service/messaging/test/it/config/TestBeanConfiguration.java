package mhalo.payment.service.messaging.test.it.config;

import mhalo.payment.service.domain.ParkingCreatedEventListenerImpl;
import mhalo.payment.service.domain.ports.input.event.listener.parking.ParkingCreatedEventListener;
import mhalo.payment.service.domain.ports.input.event.listener.parking.ParkingStoppedEventListener;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration

public class TestBeanConfiguration {

    @MockWrappedBean
    public ParkingCreatedEventListener parkingCreatedEventListener() {
        return Mockito.mock(ParkingCreatedEventListenerImpl.class);
    }

    @MockWrappedBean
    public ParkingStoppedEventListener parkingStoppedEventListener() {
        return Mockito.mock(ParkingStoppedEventListener.class);
    }
}
