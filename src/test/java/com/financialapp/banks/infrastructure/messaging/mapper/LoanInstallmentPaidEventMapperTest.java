package com.financialapp.banks.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.LoanInstallmentPaidEvent;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoanInstallmentPaidEventMapperTest {

    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private final LoanInstallmentPaidEventMapper mapper = new LoanInstallmentPaidEventMapper(objectMapper());

    @Test
    void mapsToPaymentRecordedWithInstallmentDescription() {
        LoanInstallmentPaidEvent event = new LoanInstallmentPaidEvent(
                new UserId(3L),
                "1234567890123456789012",
                new Money(new BigDecimal("200.00"), Currency.getInstance("ARS")),
                "Personal Loan",
                5,
                LocalDate.of(2026, 6, 7)
        );

        List<OutboxRecord> records = mapper.toOutboxRecords(event);

        assertThat(records).hasSize(1);
        OutboxRecord r = records.get(0);
        assertThat(r.topic()).isEqualTo("banks.payment.recorded");
        assertThat(r.key()).isEqualTo("3");
        assertThat(r.dataJson()).contains("Loan Payment: Personal Loan (Installment 5)");
        assertThat(r.dataJson()).contains("200.00");
    }
}
