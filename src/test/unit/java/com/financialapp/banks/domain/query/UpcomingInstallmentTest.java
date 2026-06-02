package com.financialapp.banks.domain.query;

import com.financialapp.banks.domain.common.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class UpcomingInstallmentTest {

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void exposesFieldsAndValueSemantics() {
        var amount = new Money(new BigDecimal("100.00"), ARS);
        var a = new UpcomingInstallment(1L, "LOAN", "Loan 1", amount, LocalDate.of(2026, 7, 1), 2, 12, false);
        var b = new UpcomingInstallment(1L, "LOAN", "Loan 1", amount, LocalDate.of(2026, 7, 1), 2, 12, false);
        var c = new UpcomingInstallment(2L, "CARD", "Card 1", amount, LocalDate.of(2026, 7, 1), 1, 6, true);

        assertThat(a.installmentId()).isEqualTo(1L);
        assertThat(a.type()).isEqualTo("LOAN");
        assertThat(a.description()).isEqualTo("Loan 1");
        assertThat(a.amount()).isEqualTo(amount);
        assertThat(a.dueDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(a.installmentNumber()).isEqualTo(2);
        assertThat(a.totalInstallments()).isEqualTo(12);
        assertThat(a.paid()).isFalse();
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(c);
        assertThat(a.toString()).contains("LOAN");
    }
}
