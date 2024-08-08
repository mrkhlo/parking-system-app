package mhalo.parking.service.dataaccess.adapter;

import lombok.RequiredArgsConstructor;
import mhalo.parking.service.dataaccess.entity.ParkingEntity;
import mhalo.parking.service.dataaccess.mapper.ParkingDataAccessMapper;
import mhalo.parking.service.dataaccess.repository.ParkingJpaRepository;
import mhalo.parking.service.domain.model.Parking;
import mhalo.parking.service.domain.ports.output.repository.ParkingRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ParkingRepositoryImpl implements ParkingRepository {

    private final ParkingJpaRepository parkingJpaRepository;
    private final ParkingDataAccessMapper parkingDataAccessMapper;


    @Override
    public Parking save(Parking parking) {
        ParkingEntity parkingEntity = parkingDataAccessMapper.mapParkingToParkingEntity(parking);
        return parkingDataAccessMapper
                .mapParkingEntityToParking(parkingJpaRepository.save(parkingEntity));
    }

    @Override
    public Optional<Parking> findById(UUID id) {
        return parkingJpaRepository.findById(id)
                .map(parkingDataAccessMapper::mapParkingEntityToParking);
    }

    @Override
    public Optional<Parking> findByTrackingId(UUID trackingId) {
        return parkingJpaRepository.findByTrackingId(trackingId)
                .map(parkingDataAccessMapper::mapParkingEntityToParking);
    }
}
