package com.financialapp.banks.infrastructure.messaging;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.commons.messaging.infrastructure.messaging.relay.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventPublisher outboxEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        outboxEventPublisher.publish(event);
    }
}
