package com.financialapp.banks.application.loan;

import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.loan.command.OriginateLoanCommand;
import com.financialapp.banks.application.loan.impl.OriginateLoanUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.loan.LoanAccountMismatchException;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.model.account.AccountNumber;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.bank.SucursalCode;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.gateway.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OriginateLoanUseCaseImplTest {

    @Mock LoanRepository loanRepository;
    @Mock BankRepository bankRepository;
    @Mock AccountRepository accountRepository;
    @Mock AdjustBalanceUseCase adjustBalance;
    @Mock DomainEventPublisher eventPublisher;
    OriginateLoanUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new OriginateLoanUseCaseImpl(loanRepository, bankRepository, accountRepository,
                adjustBalance, eventPublisher);
    }

    private CheckingAccount destAccount() {
        return new CheckingAccount(
                new Cbu(new BankNumber("007"), new SucursalCode("0001"), new AccountNumber("0000000012345")), "alias",
                new Money(BigDecimal.ZERO, Currency.getInstance("USD")),
                new UserId(1L), new BankNumber("007"), "My acc", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void create_generatesAmortizedInstallments() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(accountRepository.findByCbu("1234567890123456789012")).thenReturn(Optional.of(destAccount()));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan l = inv.getArgument(0);
            return new Loan(new LoanId(500L), l.userId(), l.bankNumber(), l.name(), l.principal(),
                    l.interestRate(), l.totalInstallments(), l.remainingInstallments(),
                    l.amortizationType(), l.startDate(), l.active(), l.installments(), l.createdAt(), l.updatedAt());
        });

        Loan result = useCase.execute(new OriginateLoanCommand(new UserId(1L), new BankNumber("007"),
                "1234567890123456789012", "Car Loan", "10000.00",
                "12.00", 12, LocalDate.of(2026, 1, 1), AmortizationType.FRENCH));

        assertThat(result.id().value()).isEqualTo(500L);

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(captor.capture());
        assertThat(captor.getValue().installments()).hasSize(12);
        verify(adjustBalance).execute(any());
        verify(eventPublisher).publishAll(any());
    }

    @Test
    void create_rejectsMismatchedBank() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        CheckingAccount otherBankAccount = new CheckingAccount(
                new Cbu(new BankNumber("072"), new SucursalCode("0001"), new AccountNumber("0000000012345")), "alias",
                new Money(BigDecimal.ZERO, Currency.getInstance("USD")),
                new UserId(1L), new BankNumber("072"), "My acc", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(accountRepository.findByCbu("1234567890123456789012")).thenReturn(Optional.of(otherBankAccount));

        assertThatThrownBy(() -> useCase.execute(new OriginateLoanCommand(new UserId(1L), new BankNumber("007"),
                "1234567890123456789012", "Car Loan", "100", "12",
                1, LocalDate.now(), AmortizationType.FRENCH)))
                .isInstanceOf(LoanAccountMismatchException.class);
    }

    @Test
    void create_throwsWhenBankMissing() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new OriginateLoanCommand(new UserId(1L), new BankNumber("007"),
                "cbu", "Loan", "100", "12", 1, LocalDate.now(), AmortizationType.FRENCH)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
