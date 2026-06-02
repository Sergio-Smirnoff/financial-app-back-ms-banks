package com.financialapp.banks.domain.gateway;

import com.financialapp.banks.domain.common.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publish(DomainEvent event);

    /** Publishes every event an aggregate recorded for the operation, in order. */
    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
