package com.financialapp.banks.infrastructure.gateway;

import com.financialapp.banks.infrastructure.persistence.entity.InboundEventEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.InboundEventJpaRepository;
import com.financialapp.commons.messaging.domain.gateway.ProcessedEventGateway;
import com.financialapp.commons.messaging.domain.model.EventId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProcessedEventGatewayJpaAdapter implements ProcessedEventGateway {

    private final InboundEventJpaRepository repository;

    @Override
    public boolean isProcessed(EventId eventId) {
        return repository.existsById(eventId.value());
    }

    @Override
    public void markProcessed(EventId eventId) {
        InboundEventEntity entity = new InboundEventEntity();
        entity.setEventId(eventId.value());
        entity.setProcessedAt(LocalDateTime.now());
        repository.save(entity);
    }
}
