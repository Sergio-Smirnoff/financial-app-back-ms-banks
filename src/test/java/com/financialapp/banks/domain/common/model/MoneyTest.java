package com.financialapp.banks.domain.common.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test void addsSameCurrency() {
        assertThat(Money.of(new BigDecimal("10.00"), "ARS").add(Money.of(new BigDecimal("5.50"), "ARS")))
            .isEqualTo(Money.of(new BigDecimal("15.50"), "ARS"));
    }

    @Test void subtractsSameCurrency() {
        assertThat(Money.of(new BigDecimal("10.00"), "ARS").subtract(Money.of(new BigDecimal("4.00"), "ARS")))
            .isEqualTo(Money.of(new BigDecimal("6.00"), "ARS"));
    }

    @Test void negates() {
        assertThat(Money.of(new BigDecimal("3.00"), "ARS").negate())
            .isEqualTo(Money.of(new BigDecimal("-3.00"), "ARS"));
    }

    @Test void isNegativeReflectsSign() {
        assertThat(Money.of(new BigDecimal("-1.00"), "ARS").isNegative()).isTrue();
        assertThat(Money.of(new BigDecimal("1.00"), "ARS").isNegative()).isFalse();
    }

    @Test void isLessThanComparesSameCurrency() {
        assertThat(Money.of(new BigDecimal("5.00"), "ARS").isLessThan(Money.of(new BigDecimal("9.00"), "ARS"))).isTrue();
        assertThat(Money.of(new BigDecimal("9.00"), "ARS").isLessThan(Money.of(new BigDecimal("5.00"), "ARS"))).isFalse();
    }

    @Test void rejectsCurrencyMismatchOnAdd() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("1"), "ARS").add(Money.of(new BigDecimal("1"), "USD")))
            .isInstanceOf(com.financialapp.banks.domain.exception.account.AccountCurrencyMismatchException.class);
    }
}
