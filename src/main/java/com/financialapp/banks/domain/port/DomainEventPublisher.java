package com.financialapp.banks.domain.port;

import com.financialapp.banks.domain.shared.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
