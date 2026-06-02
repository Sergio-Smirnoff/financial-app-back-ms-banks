package com.financialapp.banks.application.account;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.impl.AdjustBalanceUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.DomainException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.account.AccountInsufficientFundsException;
import com.financialapp.banks.domain.exception.account.AccountInvestmentRestrictionException;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountNumber;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.bank.SucursalCode;
import com.financialapp.banks.domain.gateway.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdjustBalanceUseCaseImplTest {

    @Mock AccountRepository accountRepository;
    @Mock DomainEventPublisher eventPublisher;
    AdjustBalanceUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AdjustBalanceUseCaseImpl(accountRepository, eventPublisher);
    }

    private CheckingAccount checking(BigDecimal balance) {
        return new CheckingAccount(
                new Cbu(new BankNumber("007"), new SucursalCode("0001"), new AccountNumber("0000000012345")), "alias",
                new Money(balance, Currency.getInstance("USD")),
                new UserId(1L), new BankNumber("007"), "My acc", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void adjust_creditsBalance() {
        CheckingAccount acc = checking(new BigDecimal("100.00"));
        when(accountRepository.findByCbu(acc.cbu().value())).thenReturn(Optional.of(acc));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new AdjustBalanceCommand(acc.cbu().value(),
                new Money(new BigDecimal("50.00"), Currency.getInstance("USD"))));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().balance().amount()).isEqualByComparingTo("150.00");
    }

    @Test
    void adjust_rejectsInvestmentAccount() {
        InvestmentAccount acc = new InvestmentAccount(
                new Cbu(new BankNumber("007"), new SucursalCode("0001"), new AccountNumber("0000000012345")), "alias",
                new Money(BigDecimal.TEN, Currency.getInstance("USD")),
                new UserId(1L), new BankNumber("007"), "Inv", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(accountRepository.findByCbu(acc.cbu().value())).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> useCase.execute(new AdjustBalanceCommand(acc.cbu().value(),
                new Money(BigDecimal.ONE, Currency.getInstance("USD")))))
                .isInstanceOf(AccountInvestmentRestrictionException.class)
                .hasMessageContaining("investment account");
    }

    @Test
    void adjust_rejectsInsufficientFunds() {
        CheckingAccount acc = checking(new BigDecimal("10.00"));
        when(accountRepository.findByCbu(acc.cbu().value())).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> useCase.execute(new AdjustBalanceCommand(acc.cbu().value(),
                new Money(new BigDecimal("-50.00"), Currency.getInstance("USD")))))
                .isInstanceOf(AccountInsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void adjust_throwsWhenAccountMissing() {
        when(accountRepository.findByCbu("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new AdjustBalanceCommand("missing",
                new Money(BigDecimal.ONE, Currency.getInstance("USD")))))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
