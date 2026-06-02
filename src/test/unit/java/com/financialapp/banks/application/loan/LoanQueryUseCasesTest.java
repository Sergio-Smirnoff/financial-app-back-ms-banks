package com.financialapp.banks.application.loan;

import com.financialapp.banks.application.loan.impl.CancelLoanUseCaseImpl;
import com.financialapp.banks.application.loan.impl.GetLoanInstallmentsUseCaseImpl;
import com.financialapp.banks.application.loan.impl.GetLoanUseCaseImpl;
import com.financialapp.banks.application.loan.impl.ListLoansUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the read/cancel loan use cases (one class per impl, grouped for brevity). */
@ExtendWith(MockitoExtension.class)
class LoanQueryUseCasesTest {

    @Mock LoanRepository loanRepository;

    private static final UserId USER = new UserId(1L);
    private static final LoanId ID = new LoanId(5L);

    private Loan loan() {
        return Loan.originate(USER, new BankNumber("007"), "Loan",
                new Money(new BigDecimal("200.00"), Currency.getInstance("ARS")), BigDecimal.ZERO, 2,
                AmortizationType.FRENCH, LocalDate.of(2026, 6, 1), "0070001600000000123459").loan();
    }

    // --- CancelLoan ---

    @Test
    void cancelLoan_deletesWhenFound() {
        var useCase = new CancelLoanUseCaseImpl(loanRepository);
        when(loanRepository.findByIdAndUserId(ID, USER)).thenReturn(Optional.of(loan()));

        useCase.execute(new com.financialapp.banks.domain.usecase.loan.command.CancelLoanCommand(ID, USER));

        verify(loanRepository).delete(ID);
    }

    @Test
    void cancelLoan_throwsWhenMissing() {
        var useCase = new CancelLoanUseCaseImpl(loanRepository);
        when(loanRepository.findByIdAndUserId(ID, USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new com.financialapp.banks.domain.usecase.loan.command.CancelLoanCommand(ID, USER)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(loanRepository, never()).delete(ID);
    }

    // --- GetLoan ---

    @Test
    void getLoan_returnsWhenFound() {
        var useCase = new GetLoanUseCaseImpl(loanRepository);
        Loan loan = loan();
        when(loanRepository.findByIdAndUserId(ID, USER)).thenReturn(Optional.of(loan));

        assertThat(useCase.execute(ID, USER)).isSameAs(loan);
    }

    @Test
    void getLoan_throwsWhenMissing() {
        var useCase = new GetLoanUseCaseImpl(loanRepository);
        when(loanRepository.findByIdAndUserId(ID, USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ID, USER)).isInstanceOf(ResourceNotFoundException.class);
    }

    // --- GetLoanInstallments ---

    @Test
    void getLoanInstallments_returnsScheduleWhenFound() {
        var useCase = new GetLoanInstallmentsUseCaseImpl(loanRepository);
        when(loanRepository.findByIdAndUserId(ID, USER)).thenReturn(Optional.of(loan()));

        assertThat(useCase.execute(ID, USER)).hasSize(2);
    }

    @Test
    void getLoanInstallments_throwsWhenMissing() {
        var useCase = new GetLoanInstallmentsUseCaseImpl(loanRepository);
        when(loanRepository.findByIdAndUserId(ID, USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ID, USER)).isInstanceOf(ResourceNotFoundException.class);
    }

    // --- ListLoans ---

    @Test
    void listLoans_byBankNumber_whenBankProvided() {
        var useCase = new ListLoansUseCaseImpl(loanRepository);
        var bank = new BankNumber("007");
        when(loanRepository.findByBankNumber(bank)).thenReturn(List.of(loan()));

        assertThat(useCase.execute(USER, bank)).hasSize(1);
        verify(loanRepository, never()).findByUserId(USER);
    }

    @Test
    void listLoans_byUser_whenBankNull() {
        var useCase = new ListLoansUseCaseImpl(loanRepository);
        when(loanRepository.findByUserId(USER)).thenReturn(List.of(loan()));

        assertThat(useCase.execute(USER, null)).hasSize(1);
    }
}
