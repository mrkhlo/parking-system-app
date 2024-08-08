package mhalo.domain.ports.input.event.listener;

public interface EventListener<T> {
    void process(T event);
}
