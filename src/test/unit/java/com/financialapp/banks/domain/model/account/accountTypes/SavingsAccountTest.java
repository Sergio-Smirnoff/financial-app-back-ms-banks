package com.financialapp.banks.domain.model.account.accountTypes;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class SavingsAccountTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2026, 2, 2, 0, 0);

    private SavingsAccount savings(String balance) {
        return new SavingsAccount(Cbu.from("0070001600000000123459"), "alias",
                new Money(new BigDecimal(balance), ARS), new UserId(1L),
                new BankNumber("007"), "Savings", true, CREATED, CREATED);
    }

    @Test
    void withBalance_returnsNewSavingsAccountPreservingIdentity() {
        // Given
        SavingsAccount account = savings("100.00");

        // When
        Account result = account.withBalance(new Money(new BigDecimal("250.00"), ARS), UPDATED);

        // Then
        assertThat(result).isInstanceOf(SavingsAccount.class);
        assertThat(result.balance().amount()).isEqualByComparingTo("250.00");
        assertThat(result.cbu()).isEqualTo(account.cbu());
        assertThat(result.alias()).isEqualTo("alias");
        assertThat(result.userId()).isEqualTo(account.userId());
        assertThat(result.bankNumber()).isEqualTo(account.bankNumber());
        assertThat(result.name()).isEqualTo("Savings");
        assertThat(result.isActive()).isTrue();
        assertThat(result.createdAt()).isEqualTo(CREATED);
        assertThat(result.updatedAt()).isEqualTo(UPDATED);
    }

    @Test
    void debit_allowedForSavings() {
        // Given
        SavingsAccount account = savings("100.00");

        // When
        Account result = account.debit(new Money(new BigDecimal("40.00"), ARS), UPDATED).account();

        // Then
        assertThat(result.balance().amount()).isEqualByComparingTo("60.00");
    }
}
