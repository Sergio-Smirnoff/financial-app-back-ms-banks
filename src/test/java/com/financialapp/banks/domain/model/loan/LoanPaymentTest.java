package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.loan.LoanInstallmentAlreadyPaidException;
import com.financialapp.banks.domain.model.bank.BankName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanPaymentTest {

    private static final Currency ARS = Currency.getInstance("ARS");

    private Loan twoInstallmentLoan() {
        return Loan.originate(new UserId(1L), BankName.GALICIA, "Loan",
                        new Money(new BigDecimal("200.00"), ARS), BigDecimal.ZERO, 2,
                        AmortizationType.FRENCH, LocalDate.of(2026, 6, 1))
                .withInstallmentIds(List.of(new LoanInstallmentId(10L), new LoanInstallmentId(11L)));
    }

    @Test
    void payInstallment_marks_one_paid_and_decrements_remaining() {
        Loan loan = twoInstallmentLoan();

        Loan after = loan.payInstallment(new LoanInstallmentId(10L), LocalDate.of(2026, 6, 5));

        assertThat(after.remainingInstallments()).isEqualTo(1);
        assertThat(after.active()).isTrue();
        assertThat(after.installmentBy(new LoanInstallmentId(10L)).paid()).isTrue();
        assertThat(after.installmentBy(new LoanInstallmentId(11L)).paid()).isFalse();
    }

    @Test
    void paying_last_installment_closes_loan() {
        Loan loan = twoInstallmentLoan()
                .payInstallment(new LoanInstallmentId(10L), LocalDate.of(2026, 6, 5))
                .payInstallment(new LoanInstallmentId(11L), LocalDate.of(2026, 7, 5));

        assertThat(loan.remainingInstallments()).isZero();
        assertThat(loan.active()).isFalse();
    }

    @Test
    void paying_an_already_paid_installment_fails() {
        Loan loan = twoInstallmentLoan().payInstallment(new LoanInstallmentId(10L), LocalDate.of(2026, 6, 5));
        assertThatThrownBy(() -> loan.payInstallment(new LoanInstallmentId(10L), LocalDate.of(2026, 6, 6)))
                .isInstanceOf(LoanInstallmentAlreadyPaidException.class);
    }
}
