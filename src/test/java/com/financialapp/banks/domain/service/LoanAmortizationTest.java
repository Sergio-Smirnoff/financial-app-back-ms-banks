package com.financialapp.banks.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LoanAmortizationTest {

    @Test
    void zero_rate_splits_principal_evenly() {
        BigDecimal perInstallment = LoanAmortization.frenchInstallment(
                new BigDecimal("1200.00"), BigDecimal.ZERO, 12);
        assertThat(perInstallment).isEqualByComparingTo("100.00");
    }

    @Test
    void positive_rate_uses_french_formula() {
        // 12000 principal, 12% annual, 12 installments -> ~1066.19 / month
        BigDecimal perInstallment = LoanAmortization.frenchInstallment(
                new BigDecimal("12000.00"), new BigDecimal("12"), 12);
        assertThat(perInstallment).isEqualByComparingTo("1066.19");
    }
}
