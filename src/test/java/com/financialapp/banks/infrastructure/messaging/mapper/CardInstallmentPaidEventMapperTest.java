package com.financialapp.banks.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.CardInstallmentPaidEvent;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardInstallmentPaidEventMapperTest {

    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private final CardInstallmentPaidEventMapper mapper = new CardInstallmentPaidEventMapper(objectMapper());

    @Test
    void mapsToPaymentRecordedWithCardDescription() {
        CardInstallmentPaidEvent event = new CardInstallmentPaidEvent(
                new UserId(9L),
                "1234567890123456789012",
                new Money(new BigDecimal("150.00"), Currency.getInstance("ARS")),
                "Netflix subscription",
                2,
                6,
                LocalDate.of(2026, 6, 7)
        );

        List<OutboxRecord> records = mapper.toOutboxRecords(event);

        assertThat(records).hasSize(1);
        OutboxRecord r = records.get(0);
        assertThat(r.topic()).isEqualTo("banks.payment.recorded");
        assertThat(r.key()).isEqualTo("9");
        assertThat(r.dataJson()).contains("Card Installment: Netflix subscription (2/6)");
        assertThat(r.dataJson()).contains("150.00");
    }
}
