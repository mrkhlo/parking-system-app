package mhalo.parking.service.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mhalo.domain.model.event.model.AggregateRoot;
import mhalo.domain.model.event.model.Money;
import mhalo.parking.service.domain.exception.InvalidParkingStatusException;
import mhalo.parking.service.domain.exception.ParkingDomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Parking extends AggregateRoot<UUID> {
    private final UUID customerId;
    private final UUID zoneId;
    private final UUID trackingId;
    private final String licensePlateNumber;
    private final Money startingFee;
    private final Instant startedAt;

    private Instant stoppedAt;
    private Money closingFee;
    private ParkingStatus parkingStatus;

    public void approveParkingCreation() {
        if (parkingStatus != ParkingStatus.CREATE_PENDING) {
            throw new InvalidParkingStatusException(parkingStatus, ParkingStatus.CREATE_PENDING, "Invalid status for creation approval.");
        }

        parkingStatus = ParkingStatus.CREATED;
    }

    public void declineParkingCreation() {
        if (parkingStatus != ParkingStatus.CREATE_PENDING) {
            throw new InvalidParkingStatusException(parkingStatus, ParkingStatus.CREATE_PENDING, "Invalid status for creation declining.");
        }

        parkingStatus = ParkingStatus.CANCELLED;
    }

    public void stopParking(Instant stoppedAt, Money closingFee) {
        Objects.requireNonNull(stoppedAt, "stoppedAt cannot be null");
        Objects.requireNonNull(closingFee, "closingFee cannot be null");

        if (parkingStatus != ParkingStatus.CREATED) {
            throw new InvalidParkingStatusException(parkingStatus, ParkingStatus.CREATED, "Invalid status for stop parking.");
        }

        this.stoppedAt = stoppedAt;
        this.closingFee = closingFee;
        boolean isRefundEligible = !this.startingFee.equals(closingFee);
        this.parkingStatus = isRefundEligible ? ParkingStatus.STOP_PENDING : ParkingStatus.STOPPED;
    }

    public void approveStopParking() {
        if (parkingStatus != ParkingStatus.STOP_PENDING) {
            throw new InvalidParkingStatusException(parkingStatus, ParkingStatus.STOP_PENDING, "Invalid status for stop approval.");
        }
        parkingStatus = ParkingStatus.STOPPED;
    }

    private void validateInvariants() {
        switch (parkingStatus) {
            case CREATE_PENDING, CREATED -> validateInitialFieldsNotNull(parkingStatus);
            case STOP_PENDING, STOPPED -> validateStopState(parkingStatus);
        }
    }

    private void validateInitialFieldsNotNull(ParkingStatus parkingStatus) {
        if (parkingStatus == null || getId() == null || customerId == null || zoneId == null || trackingId == null ||
                licensePlateNumber == null || startingFee == null || startedAt == null)
            throw new ParkingDomainException("Parking invariants are not met for for status: " + parkingStatus);
    }

    private void validateStopState(ParkingStatus parkingStatus) {
        validateInitialFieldsNotNull(parkingStatus);
        if (stoppedAt == null || closingFee == null) {
            throw new ParkingDomainException("Parking invariants are not met for status: " + parkingStatus);
        }
    }

    /**
     * Builder which supposed to be used only for creating parking with its initial CREATE_PENDING state,
     * rehydrating parking from the database and creating test data.
     * Since this way, multiple state changes, hence invariant checks can be "skipped",
     * validating all invariants here is a must.
     */
    private Parking(Builder builder) {
        setId(builder.id);
        customerId = builder.customerId;
        zoneId = builder.zoneId;
        trackingId = builder.trackingId;
        licensePlateNumber = builder.licensePlateNumber;
        startingFee = builder.startingFee;
        startedAt = builder.startedAt;
        stoppedAt = builder.stoppedAt;
        closingFee = builder.closingFee;
        parkingStatus = builder.parkingStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID id;
        private UUID customerId;
        private UUID zoneId;
        private UUID trackingId;
        private String licensePlateNumber;
        private Money startingFee;
        private Instant startedAt;
        private Instant stoppedAt;
        private Money closingFee;
        private ParkingStatus parkingStatus;

        private Builder() {
        }

        public Builder id(UUID val) {
            id = val;
            return this;
        }

        public Builder customerId(UUID val) {
            customerId = val;
            return this;
        }

        public Builder zoneId(UUID val) {
            zoneId = val;
            return this;
        }

        public Builder trackingId(UUID val) {
            trackingId = val;
            return this;
        }

        public Builder licensePlateNumber(String val) {
            licensePlateNumber = val;
            return this;
        }

        public Builder startingFee(Money val) {
            startingFee = val;
            return this;
        }

        public Builder startedAt(Instant val) {
            startedAt = val;
            return this;
        }

        public Builder stoppedAt(Instant val) {
            stoppedAt = val;
            return this;
        }

        public Builder closingFee(Money val) {
            closingFee = val;
            return this;
        }

        public Builder parkingStatus(ParkingStatus val) {
            parkingStatus = val;
            return this;
        }

        public Parking build() {
            Parking parking = new Parking(this);
            parking.validateInvariants();
            return parking;
        }
    }
}
