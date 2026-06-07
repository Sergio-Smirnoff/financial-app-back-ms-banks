package com.financialapp.banks.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.infrastructure.messaging.payload.LowBalanceData;
import com.financialapp.commons.messaging.domain.gateway.TypedDomainEventMapper;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LowBalanceEventMapper extends TypedDomainEventMapper<LowBalanceEvent> {

    private static final String TOPIC = "banks.account.low_balance";
    private static final String SCHEMA = "https://schemas.financial-app/banks/account-low-balance/v1";
    private static final String SOURCE = "ms-banks";

    private final ObjectMapper objectMapper;

    public LowBalanceEventMapper(ObjectMapper objectMapper) {
        super(LowBalanceEvent.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected List<OutboxRecord> mapTyped(LowBalanceEvent event) {
        LowBalanceData data = new LowBalanceData(
                event.userId().value(),
                event.accountName(),
                event.accountCbu(),
                event.bankNumber().value(),
                event.balance().amount(),
                event.balance().currency().getCurrencyCode()
        );
        return List.of(OutboxRecord.create(TOPIC, event.userId().value().toString(),
                new EventType(TOPIC), SOURCE, SCHEMA, serialize(data)));
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize LowBalanceEvent data", ex);
        }
    }
}
