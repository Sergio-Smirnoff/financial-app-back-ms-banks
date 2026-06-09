package com.financialapp.banks.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.LoanCreatedEvent;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoanCreatedEventMapperTest {

    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private final LoanCreatedEventMapper mapper = new LoanCreatedEventMapper(objectMapper());

    @Test
    void mapsToPaymentRecordedOutboxRecord() {
        LoanCreatedEvent event = new LoanCreatedEvent(
                new UserId(7L),
                "1234567890123456789012",
                new Money(new BigDecimal("5000.00"), Currency.getInstance("ARS")),
                "Car Loan",
                LocalDate.of(2026, 6, 7)
        );

        List<OutboxRecord> records = mapper.toOutboxRecords(event);

        assertThat(records).hasSize(1);
        OutboxRecord r = records.get(0);
        assertThat(r.topic()).isEqualTo("banks.payment.recorded");
        assertThat(r.type().value()).isEqualTo("banks.payment.recorded");
        assertThat(r.key()).isEqualTo("7");
        assertThat(r.source()).isEqualTo("ms-banks");
        assertThat(r.dataJson()).contains("Loan Deposit: Car Loan");
        assertThat(r.dataJson()).contains("5000.00");
        assertThat(r.dataJson()).contains("ARS");
    }

    @Test
    void supportsLoanCreatedEventOnly() {
        assertThat(mapper.supports(new LoanCreatedEvent(
                new UserId(1L), "cbu",
                new Money(BigDecimal.ONE, Currency.getInstance("ARS")),
                "name", LocalDate.now()))).isTrue();
        assertThat(mapper.supports("not-an-event")).isFalse();
    }
}
