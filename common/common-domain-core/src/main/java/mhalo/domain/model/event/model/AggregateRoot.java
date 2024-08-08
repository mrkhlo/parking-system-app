package mhalo.domain.model.event.model;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class AggregateRoot<ID> extends BaseEntity<ID> {
}
