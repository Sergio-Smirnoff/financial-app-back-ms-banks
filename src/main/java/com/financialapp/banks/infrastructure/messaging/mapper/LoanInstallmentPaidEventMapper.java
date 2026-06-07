package com.financialapp.banks.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.event.LoanInstallmentPaidEvent;
import com.financialapp.banks.infrastructure.messaging.payload.PaymentRecordedData;
import com.financialapp.commons.messaging.domain.gateway.TypedDomainEventMapper;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoanInstallmentPaidEventMapper extends TypedDomainEventMapper<LoanInstallmentPaidEvent> {

    private static final String TOPIC = "banks.payment.recorded";
    private static final String SCHEMA = "https://schemas.financial-app/banks/payment-recorded/v1";
    private static final String SOURCE = "ms-banks";

    private final ObjectMapper objectMapper;

    public LoanInstallmentPaidEventMapper(ObjectMapper objectMapper) {
        super(LoanInstallmentPaidEvent.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected List<OutboxRecord> mapTyped(LoanInstallmentPaidEvent event) {
        PaymentRecordedData data = new PaymentRecordedData(
                event.userId().value(),
                event.accountCbu(),
                event.amount().amount(),
                event.amount().currency().getCurrencyCode(),
                "Loan Payment: " + event.loanName() + " (Installment " + event.installmentNumber() + ")",
                event.paidDate()
        );
        return List.of(OutboxRecord.create(TOPIC, event.userId().value().toString(),
                new EventType(TOPIC), SOURCE, SCHEMA, serialize(data)));
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize LoanInstallmentPaidEvent data", ex);
        }
    }
}
