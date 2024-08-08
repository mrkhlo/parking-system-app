package mhalo.parking.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import mhalo.domain.model.event.model.AggregateRoot;
import mhalo.domain.model.event.model.Interval;
import mhalo.domain.model.event.model.Rate;

import java.util.UUID;

@AllArgsConstructor
@ToString
@Getter
public class Zone extends AggregateRoot<UUID> {
    private final Rate rate;
    private final Interval payInterval;

    private Zone(Builder builder) {
        setId(builder.id);
        rate = builder.rate;
        payInterval = builder.payInterval;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Zone copy) {
        Builder builder = new Builder();
        builder.id = copy.getId();
        builder.rate = copy.getRate();
        builder.payInterval = copy.getPayInterval();
        return builder;
    }

    public static final class Builder {
        private UUID id;
        private Rate rate;
        private Interval payInterval;

        private Builder() {
        }

        public Builder id(UUID val) {
            id = val;
            return this;
        }

        public Builder rate(Rate val) {
            rate = val;
            return this;
        }

        public Builder payInterval(Interval val) {
            payInterval = val;
            return this;
        }

        public Zone build() {
            return new Zone(this);
        }
    }
}
