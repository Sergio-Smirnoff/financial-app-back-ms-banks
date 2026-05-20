package com.financialapp.banks.domain.port;

import com.financialapp.banks.domain.common.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
