package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.loan.LoanInstallmentAlreadyPaidException;
import com.financialapp.banks.domain.exception.loan.LoanInstallmentMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanInstallmentTest {

    private LoanInstallment unpaid() {
        return new LoanInstallment(
                new LoanInstallmentId(10L),
                new LoanId(1L),
                3,
                new Money(new BigDecimal("100.00"), Currency.getInstance("USD")),
                LocalDate.of(2026, 5, 1),
                false,
                null,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

    @Test
    void pay_marksPaidAndKeepsImmutableSource() {
        LoanInstallment source = unpaid();
        LocalDate paidDate = LocalDate.of(2026, 5, 2);

        LoanInstallment paid = source.pay(paidDate);

        assertThat(paid.paid()).isTrue();
        assertThat(paid.paidDate()).isEqualTo(paidDate);
        assertThat(paid.updatedAt()).isAfter(source.updatedAt());
        // unchanged components
        assertThat(paid.id()).isEqualTo(source.id());
        assertThat(paid.loanId()).isEqualTo(source.loanId());
        assertThat(paid.installmentNumber()).isEqualTo(source.installmentNumber());
        assertThat(paid.amount()).isEqualTo(source.amount());
        assertThat(paid.dueDate()).isEqualTo(source.dueDate());
        assertThat(paid.createdAt()).isEqualTo(source.createdAt());
        // source untouched (immutability)
        assertThat(source.paid()).isFalse();
        assertThat(source.paidDate()).isNull();
    }

    @Test
    void pay_throwsWhenAlreadyPaid() {
        LoanInstallment alreadyPaid = unpaid().pay(LocalDate.of(2026, 5, 2));

        assertThatThrownBy(() -> alreadyPaid.pay(LocalDate.of(2026, 5, 3)))
                .isInstanceOf(LoanInstallmentAlreadyPaidException.class);
    }

    @Test
    void ensureBelongsTo_noopWhenMatches() {
        unpaid().ensureBelongsTo(new LoanId(1L));
    }

    @Test
    void ensureBelongsTo_throwsWhenMismatch() {
        assertThatThrownBy(() -> unpaid().ensureBelongsTo(new LoanId(99L)))
                .isInstanceOf(LoanInstallmentMismatchException.class);
    }
}
