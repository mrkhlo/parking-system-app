package mhalo.parking.service.dataaccess.mapper;

import mhalo.domain.model.event.model.Money;
import mhalo.parking.service.dataaccess.entity.ParkingEntity;
import mhalo.parking.service.domain.model.Parking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ParkingDataAccessMapper {
    public ParkingEntity mapParkingToParkingEntity(Parking parking) {
        BigDecimal closingFee = parking.getClosingFee() != null ?
                parking.getClosingFee().getAmount() :
                null;
        return ParkingEntity.builder()
                .id(parking.getId())
                .trackingId(parking.getTrackingId())
                .customerId(parking.getCustomerId())
                .zoneId(parking.getZoneId())
                .licensePlateNumber(parking.getLicensePlateNumber())
                .closingFee(closingFee)
                .startingFee(parking.getStartingFee().getAmount())
                .startedAt(parking.getStartedAt())
                .stoppedAt(parking.getStoppedAt())
                .parkingStatus(parking.getParkingStatus())
                .build();
    }

    public Parking mapParkingEntityToParking(ParkingEntity parkingEntity) {
        Money money = parkingEntity.getClosingFee() != null ?
                new Money(parkingEntity.getClosingFee()) :
                null;
        return Parking.builder()
                .id(parkingEntity.getId())
                .trackingId(parkingEntity.getTrackingId())
                .customerId(parkingEntity.getCustomerId())
                .zoneId(parkingEntity.getZoneId())
                .licensePlateNumber(parkingEntity.getLicensePlateNumber())
                .closingFee(money)
                .startingFee(new Money(parkingEntity.getStartingFee()))
                .startedAt(parkingEntity.getStartedAt())
                .stoppedAt(parkingEntity.getStoppedAt())
                .parkingStatus(parkingEntity.getParkingStatus())
                .build();
    }
}
