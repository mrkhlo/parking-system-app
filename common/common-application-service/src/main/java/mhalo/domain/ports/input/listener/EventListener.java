package mhalo.domain.ports.input.listener;

public interface EventListener<T> {
    void process(T event);
}
