package mhalo.outbox;

public interface OutboxScheduler {
    void processOutboxMessage();
}
