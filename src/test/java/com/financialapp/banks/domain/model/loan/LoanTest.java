package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.loan.LoanAlreadyClosedException;
import com.financialapp.banks.domain.model.bank.BankName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanTest {

    private Loan loan(int remainingInstallments, boolean active) {
        return new Loan(
                new LoanId(1L),
                new UserId(1L),
                BankName.GALICIA,
                "Car Loan",
                new Money(new BigDecimal("10000.00"), Currency.getInstance("USD")),
                new BigDecimal("12.00"),
                12,
                remainingInstallments,
                AmortizationType.FRENCH,
                LocalDate.of(2026, 1, 1),
                active,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

    @Test
    void ensureActive_noopWhenActive() {
        loan(5, true).ensureActive();
    }

    @Test
    void ensureActive_throwsWhenClosed() {
        assertThatThrownBy(() -> loan(0, false).ensureActive())
                .isInstanceOf(LoanAlreadyClosedException.class);
    }

    @Test
    void registerInstallmentPaid_decrementsAndStaysActiveWhenRemaining() {
        Loan source = loan(3, true);

        Loan updated = source.registerInstallmentPaid();

        assertThat(updated.remainingInstallments()).isEqualTo(2);
        assertThat(updated.active()).isTrue();
        assertThat(updated.updatedAt()).isAfter(source.updatedAt());
        // unchanged components
        assertThat(updated.id()).isEqualTo(source.id());
        assertThat(updated.userId()).isEqualTo(source.userId());
        assertThat(updated.bankName()).isEqualTo(source.bankName());
        assertThat(updated.name()).isEqualTo(source.name());
        assertThat(updated.principal()).isEqualTo(source.principal());
        assertThat(updated.interestRate()).isEqualTo(source.interestRate());
        assertThat(updated.totalInstallments()).isEqualTo(source.totalInstallments());
        assertThat(updated.amortizationType()).isEqualTo(source.amortizationType());
        assertThat(updated.startDate()).isEqualTo(source.startDate());
        assertThat(updated.createdAt()).isEqualTo(source.createdAt());
        // immutability
        assertThat(source.remainingInstallments()).isEqualTo(3);
    }

    @Test
    void registerInstallmentPaid_closesLoanWhenLastInstallment() {
        Loan source = loan(1, true);

        Loan updated = source.registerInstallmentPaid();

        assertThat(updated.remainingInstallments()).isEqualTo(0);
        assertThat(updated.active()).isFalse();
    }
}
