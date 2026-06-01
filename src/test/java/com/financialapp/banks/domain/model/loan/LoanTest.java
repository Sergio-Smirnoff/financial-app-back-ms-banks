package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.loan.LoanAlreadyClosedException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanTest {

    private Loan loan(int remainingInstallments, boolean active) {
        return new Loan(
                new LoanId(1L),
                new UserId(1L),
                new BankNumber("007"),
                "Car Loan",
                new Money(new BigDecimal("10000.00"), Currency.getInstance("USD")),
                new BigDecimal("12.00"),
                12,
                remainingInstallments,
                AmortizationType.FRENCH,
                LocalDate.of(2026, 1, 1),
                active,
                List.of(),
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
}
