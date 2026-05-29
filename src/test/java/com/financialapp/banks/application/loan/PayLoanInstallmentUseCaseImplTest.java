package com.financialapp.banks.application.loan;

import com.financialapp.banks.application.loan.impl.PayLoanInstallmentUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.LoanInstallmentPaidEvent;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.loan.LoanAlreadyClosedException;
import com.financialapp.banks.domain.exception.loan.LoanInstallmentAlreadyPaidException;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.loan.command.PayLoanInstallmentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayLoanInstallmentUseCaseImplTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock LoanRepository loanRepository;
    @Mock AdjustBalanceUseCase adjustBalance;
    @Mock DomainEventPublisher eventPublisher;
    PayLoanInstallmentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new PayLoanInstallmentUseCaseImpl(loanRepository, adjustBalance, eventPublisher);
    }

    /** Loan with one unpaid installment id=20 (number 3, 100 USD) and the given remaining/active. */
    private Loan loan(int remaining, boolean active, boolean installmentPaid) {
        LoanInstallment installment = new LoanInstallment(new LoanInstallmentId(20L), new LoanId(1L), 3,
                new Money(new BigDecimal("100.00"), USD), LocalDate.of(2026, 5, 1),
                installmentPaid, installmentPaid ? LocalDate.of(2026, 4, 30) : null,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));
        return new Loan(new LoanId(1L), new UserId(7L), BankName.GALICIA, "Car Loan",
                new Money(new BigDecimal("10000.00"), USD), new BigDecimal("12.00"),
                12, remaining, AmortizationType.FRENCH, LocalDate.of(2026, 1, 1), active,
                List.of(installment), LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    private PayLoanInstallmentCommand cmd(LocalDate paidDate) {
        return new PayLoanInstallmentCommand(new LoanId(1L), new LoanInstallmentId(20L),
                new UserId(7L), "1234567890123456789012", paidDate);
    }

    @Test
    void pay_debitsSavesAndPublishesEvent() {
        LocalDate paidDate = LocalDate.of(2026, 5, 2);
        when(loanRepository.findByIdAndUserId(new LoanId(1L), new UserId(7L)))
                .thenReturn(Optional.of(loan(3, true, false)));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanInstallment result = useCase.execute(cmd(paidDate));

        assertThat(result.paid()).isTrue();
        assertThat(result.paidDate()).isEqualTo(paidDate);

        // debit is a negative-amount adjustment on the account
        ArgumentCaptor<AdjustBalanceCommand> adjustCaptor = ArgumentCaptor.forClass(AdjustBalanceCommand.class);
        verify(adjustBalance).execute(adjustCaptor.capture());
        assertThat(adjustCaptor.getValue().delta().amount()).isEqualByComparingTo("-100.00");

        // loan saved with decremented remaining
        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(loanCaptor.capture());
        assertThat(loanCaptor.getValue().remainingInstallments()).isEqualTo(2);
        assertThat(loanCaptor.getValue().active()).isTrue();

        // event is recorded by the aggregate and drained via publishAll
        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.List<com.financialapp.banks.domain.common.DomainEvent>> eventCaptor =
                ArgumentCaptor.forClass(java.util.List.class);
        verify(eventPublisher).publishAll(eventCaptor.capture());
        LoanInstallmentPaidEvent event = (LoanInstallmentPaidEvent) eventCaptor.getValue().get(0);
        assertThat(event.amount().amount()).isEqualByComparingTo("-100.00");
        assertThat(event.installmentNumber()).isEqualTo(3);
        assertThat(event.paidDate()).isEqualTo(paidDate);
    }

    @Test
    void pay_defaultsPaidDateToToday() {
        when(loanRepository.findByIdAndUserId(new LoanId(1L), new UserId(7L)))
                .thenReturn(Optional.of(loan(3, true, false)));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanInstallment result = useCase.execute(cmd(null));

        assertThat(result.paidDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void pay_throwsWhenLoanMissing() {
        when(loanRepository.findByIdAndUserId(new LoanId(1L), new UserId(7L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(cmd(LocalDate.now())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(adjustBalance, never()).execute(any());
    }

    @Test
    void pay_throwsWhenLoanClosed() {
        when(loanRepository.findByIdAndUserId(new LoanId(1L), new UserId(7L)))
                .thenReturn(Optional.of(loan(0, false, false)));

        assertThatThrownBy(() -> useCase.execute(cmd(LocalDate.now())))
                .isInstanceOf(LoanAlreadyClosedException.class);
        verify(adjustBalance, never()).execute(any());
    }

    @Test
    void pay_throwsWhenInstallmentNotOnLoan() {
        Loan loanWithoutTarget = new Loan(new LoanId(1L), new UserId(7L), BankName.GALICIA, "Car Loan",
                new Money(new BigDecimal("10000.00"), USD), new BigDecimal("12.00"),
                12, 3, AmortizationType.FRENCH, LocalDate.of(2026, 1, 1), true,
                List.of(), LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));
        when(loanRepository.findByIdAndUserId(new LoanId(1L), new UserId(7L)))
                .thenReturn(Optional.of(loanWithoutTarget));

        assertThatThrownBy(() -> useCase.execute(cmd(LocalDate.now())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(adjustBalance, never()).execute(any());
    }

    @Test
    void pay_throwsWhenAlreadyPaid_andDoesNotDebit() {
        when(loanRepository.findByIdAndUserId(new LoanId(1L), new UserId(7L)))
                .thenReturn(Optional.of(loan(3, true, true)));

        assertThatThrownBy(() -> useCase.execute(cmd(LocalDate.now())))
                .isInstanceOf(LoanInstallmentAlreadyPaidException.class);
        verify(adjustBalance, never()).execute(any());
        verify(loanRepository, never()).save(any());
    }
}
