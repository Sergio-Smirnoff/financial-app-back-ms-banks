package com.financialapp.banks.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.infrastructure.messaging.payload.BalanceAdjustedData;
import com.financialapp.commons.messaging.domain.gateway.TypedDomainEventMapper;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BalanceAdjustedEventMapper extends TypedDomainEventMapper<BalanceAdjustedEvent> {

    private static final String TOPIC = "banks.account.balance_adjusted";
    private static final String SCHEMA = "https://schemas.financial-app/banks/account-balance-adjusted/v1";
    private static final String SOURCE = "ms-banks";

    private final ObjectMapper objectMapper;

    public BalanceAdjustedEventMapper(ObjectMapper objectMapper) {
        super(BalanceAdjustedEvent.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected List<OutboxRecord> mapTyped(BalanceAdjustedEvent event) {
        boolean credit = event.delta().amount().signum() >= 0;
        BalanceAdjustedData data = new BalanceAdjustedData(
                event.userId().value(),
                event.accountName(),
                event.accountCbu(),
                event.bankNumber().value(),
                event.delta().amount().abs(),
                event.delta().currency().getCurrencyCode(),
                credit
        );
        return List.of(OutboxRecord.create(TOPIC, event.userId().value().toString(),
                new EventType(TOPIC), SOURCE, SCHEMA, serialize(data)));
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize BalanceAdjustedEvent data", ex);
        }
    }
}
