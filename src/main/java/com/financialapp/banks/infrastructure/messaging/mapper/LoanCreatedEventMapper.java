package com.financialapp.banks.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.event.LoanCreatedEvent;
import com.financialapp.banks.infrastructure.messaging.payload.PaymentRecordedData;
import com.financialapp.commons.messaging.domain.gateway.TypedDomainEventMapper;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoanCreatedEventMapper extends TypedDomainEventMapper<LoanCreatedEvent> {

    private static final String TOPIC = "banks.payment.recorded";
    private static final String SCHEMA = "https://schemas.financial-app/banks/payment-recorded/v1";
    private static final String SOURCE = "ms-banks";

    private final ObjectMapper objectMapper;

    public LoanCreatedEventMapper(ObjectMapper objectMapper) {
        super(LoanCreatedEvent.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected List<OutboxRecord> mapTyped(LoanCreatedEvent event) {
        PaymentRecordedData data = new PaymentRecordedData(
                event.userId().value(),
                event.destinationAccountCbu(),
                event.amount().amount(),
                event.amount().currency().getCurrencyCode(),
                "Loan Deposit: " + event.loanName(),
                event.date()
        );
        return List.of(OutboxRecord.create(TOPIC, event.userId().value().toString(),
                new EventType(TOPIC), SOURCE, SCHEMA, serialize(data)));
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize LoanCreatedEvent data", ex);
        }
    }
}
