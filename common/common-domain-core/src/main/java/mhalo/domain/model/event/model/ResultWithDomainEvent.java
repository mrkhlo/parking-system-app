package mhalo.domain.model.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ResultWithDomainEvent<T, S> {
    private T result;
    private S event;
}
