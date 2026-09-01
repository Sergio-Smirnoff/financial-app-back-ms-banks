package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.model.account.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DebitCreditTaxTest {

    private final DebitCreditTax taxService = new DebitCreditTax();

    @Test
    void rate_checkingAccount_returnsZeroPointZeroZeroSix() {
        BigDecimal rate = taxService.rate(AccountType.CHECKING);
        assertThat(rate).isEqualByComparingTo("0.006");
    }

    @Test
    void rate_savingsAccount_returnsZero() {
        BigDecimal rate = taxService.rate(AccountType.SAVINGS);
        assertThat(rate).isEqualByComparingTo("0");
    }
}
