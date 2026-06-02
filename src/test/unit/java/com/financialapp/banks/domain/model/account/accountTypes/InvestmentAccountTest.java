package com.financialapp.banks.domain.model.account.accountTypes;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.account.AccountInvestmentRestrictionException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvestmentAccountTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2026, 2, 2, 0, 0);

    private InvestmentAccount investment(String balance) {
        return new InvestmentAccount(Cbu.from("0070001600000000123459"), "alias",
                new Money(new BigDecimal(balance), ARS), new UserId(1L),
                new BankNumber("007"), "Investment", true, CREATED, CREATED);
    }

    @Test
    void withBalance_returnsNewInvestmentAccountPreservingIdentity() {
        // Given
        InvestmentAccount account = investment("100.00");

        // When
        Account result = account.withBalance(new Money(new BigDecimal("777.00"), ARS), UPDATED);

        // Then
        assertThat(result).isInstanceOf(InvestmentAccount.class);
        assertThat(result.balance().amount()).isEqualByComparingTo("777.00");
        assertThat(result.cbu()).isEqualTo(account.cbu());
        assertThat(result.name()).isEqualTo("Investment");
        assertThat(result.createdAt()).isEqualTo(CREATED);
        assertThat(result.updatedAt()).isEqualTo(UPDATED);
    }

    @Test
    void ensureNotInvestmentRestricted_throwsViaDebit() {
        // Given
        InvestmentAccount account = investment("100.00");

        // When / Then
        assertThatThrownBy(() -> account.debit(new Money(new BigDecimal("10.00"), ARS), UPDATED))
                .isInstanceOf(AccountInvestmentRestrictionException.class)
                .hasMessageContaining("0070001600000000123459");
    }
}
