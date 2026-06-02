package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers both sides of the low-balance branch in {@code Account.adjustmentWith}:
 * a high resulting balance records ONLY a BalanceAdjustedEvent, while a low one
 * additionally records a LowBalanceEvent.
 */
class AccountAdjustmentEventsTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 10, 0);

    private Account checking(String balance) {
        return Account.create(AccountType.CHECKING, Cbu.from("0070001600000000123459"), "alias",
                new Money(new BigDecimal(balance), ARS), new UserId(1L), new BankNumber("007"),
                "Main", true, NOW, NOW);
    }

    @Test
    void credit_keepingBalanceAboveThreshold_recordsOnlyBalanceAdjustedEvent() {
        // Given a healthy balance / When crediting so the result stays >= 500
        var adjustment = checking("1000.00").credit(new Money(new BigDecimal("100.00"), ARS), NOW);

        // Then no low-balance event is recorded (the false branch)
        assertThat(adjustment.events()).hasSize(1);
        assertThat(adjustment.events().get(0)).isInstanceOf(BalanceAdjustedEvent.class);
    }

    @Test
    void debit_droppingBalanceBelowThreshold_alsoRecordsLowBalanceEvent() {
        // Given a healthy balance / When debiting so the result falls below 500
        var adjustment = checking("1000.00").debit(new Money(new BigDecimal("600.00"), ARS), NOW);

        // Then both a balance-adjusted and a low-balance event are recorded (the true branch)
        assertThat(adjustment.events()).hasSize(2);
        assertThat(adjustment.events()).hasAtLeastOneElementOfType(BalanceAdjustedEvent.class);
        assertThat(adjustment.events()).hasAtLeastOneElementOfType(LowBalanceEvent.class);
    }
}
