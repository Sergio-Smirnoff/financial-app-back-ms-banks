package com.financialapp.banks.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceAdjustedEventMapperTest {

    private final BalanceAdjustedEventMapper mapper = new BalanceAdjustedEventMapper(new ObjectMapper());

    @Test
    void creditEvent_hasCreditTrueAndAbsoluteAmount() {
        BalanceAdjustedEvent event = new BalanceAdjustedEvent(
                new UserId(5L),
                "1234567890123456789012",
                new BankNumber("072"),
                "My Savings",
                new Money(new BigDecimal("300.00"), Currency.getInstance("ARS"))
        );

        List<OutboxRecord> records = mapper.toOutboxRecords(event);

        assertThat(records).hasSize(1);
        OutboxRecord r = records.get(0);
        assertThat(r.topic()).isEqualTo("banks.account.balance_adjusted");
        assertThat(r.key()).isEqualTo("5");
        assertThat(r.dataJson()).contains("\"credit\":true");
        assertThat(r.dataJson()).contains("300.00");
        assertThat(r.dataJson()).contains("ARS");
    }

    @Test
    void debitEvent_hasCreditFalseAndAbsoluteAmount() {
        BalanceAdjustedEvent event = new BalanceAdjustedEvent(
                new UserId(5L),
                "1234567890123456789012",
                new BankNumber("072"),
                "My Savings",
                new Money(new BigDecimal("-200.00"), Currency.getInstance("ARS"))
        );

        List<OutboxRecord> records = mapper.toOutboxRecords(event);

        OutboxRecord r = records.get(0);
        assertThat(r.dataJson()).contains("\"credit\":false");
        assertThat(r.dataJson()).contains("200.00");
    }
}
