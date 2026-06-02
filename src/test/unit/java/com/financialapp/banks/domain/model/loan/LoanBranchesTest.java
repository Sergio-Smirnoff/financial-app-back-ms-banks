package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.loan.LoanAlreadyClosedException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Covers the remaining Loan branches not exercised by LoanTest / LoanPaymentTest. */
class LoanBranchesTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final String CBU = "0070001600000000123459";

    private Loan twoInstallmentLoan() {
        return Loan.originate(new UserId(1L), new BankNumber("007"), "Loan",
                new Money(new BigDecimal("200.00"), ARS), BigDecimal.ZERO, 2,
                AmortizationType.FRENCH, LocalDate.of(2026, 6, 1), CBU).loan();
    }

    @Test
    void compactConstructor_defaultsNullInstallmentsToEmptyList() {
        // Given installments == null / When constructed / Then the null branch yields an empty list
        Loan loan = new Loan(new LoanId(1L), new UserId(1L), new BankNumber("007"), "L",
                new Money(new BigDecimal("100.00"), ARS), BigDecimal.ZERO, 1, 1,
                AmortizationType.FRENCH, LocalDate.of(2026, 6, 1), true, null,
                LocalDateTime.now(), LocalDateTime.now());
        assertThat(loan.installments()).isEmpty();
    }

    @Test
    void ensureActive_throwMessageUsesIdValue_whenIdNotNull() {
        // Given a closed loan with a concrete id (covers the non-null id branch of the message)
        Loan base = twoInstallmentLoan();
        Loan closed = new Loan(new LoanId(7L), base.userId(), base.bankNumber(), base.name(),
                base.principal(), base.interestRate(), base.totalInstallments(),
                base.remainingInstallments(), base.amortizationType(), base.startDate(), false,
                base.installments(), base.createdAt(), base.updatedAt());

        assertThatThrownBy(closed::ensureActive)
                .isInstanceOf(LoanAlreadyClosedException.class)
                .hasMessageContaining("7");
    }

    @Test
    void ensureActive_throwMessageUsesNewLiteral_whenIdNull() {
        // Given a closed loan whose id is still null (covers the "new" branch of the message)
        Loan base = twoInstallmentLoan();
        Loan closed = new Loan(new LoanId(null), base.userId(), base.bankNumber(), base.name(),
                base.principal(), base.interestRate(), base.totalInstallments(),
                base.remainingInstallments(), base.amortizationType(), base.startDate(), false,
                base.installments(), base.createdAt(), base.updatedAt());

        assertThatThrownBy(closed::ensureActive)
                .isInstanceOf(LoanAlreadyClosedException.class)
                .hasMessageContaining("new");
    }

    @Test
    void installmentBy_unknownNonNullId_throwsNotFoundWithIdValue() {
        // Given installments carry concrete ids / When querying a non-existent non-null id
        Loan loan = twoInstallmentLoan().withInstallmentIds(List.of(new LoanInstallmentId(10L), new LoanInstallmentId(20L)));

        assertThatThrownBy(() -> loan.installmentBy(new LoanInstallmentId(999L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void installmentBy_unknownNullId_throwsNotFoundWithNewLiteral() {
        // Given installments carry concrete ids / When querying a null id (covers the "new" branch)
        Loan loan = twoInstallmentLoan().withInstallmentIds(List.of(new LoanInstallmentId(10L), new LoanInstallmentId(20L)));

        assertThatThrownBy(() -> loan.installmentBy(new LoanInstallmentId(null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("new");
    }
}
