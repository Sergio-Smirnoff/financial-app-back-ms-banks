package com.financialapp.banks.application.account;

import com.financialapp.banks.domain.usecase.account.command.OpenAccountCommand;
import com.financialapp.banks.application.account.impl.OpenAccountUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAccountUseCaseImplTest {

    @Mock BankRepository bankRepository;
    @Mock AccountRepository accountRepository;
    OpenAccountUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new OpenAccountUseCaseImpl(accountRepository, bankRepository);
    }

    private OpenAccountCommand command(AccountType type) {
        return new OpenAccountCommand(
                new UserId(1L), new BankNumber("007"), "Savings", type,
                new Money(new BigDecimal("100.00"), Currency.getInstance("USD")),
                true, "0070001600000000123459", "alias");
    }

    @Test
    void create_persistsSavingsAccount() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(accountRepository.existsByUserIdAndBankNumberAndName(new UserId(1L), new BankNumber("007"), "Savings")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(command(AccountType.SAVINGS));

        assertThat(result.type()).isEqualTo(AccountType.SAVINGS);
        assertThat(result.balance().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void create_rejectsDuplicateNameForSameUser() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(accountRepository.existsByUserIdAndBankNumberAndName(new UserId(1L), new BankNumber("007"), "Savings")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command(AccountType.SAVINGS)))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_allowsSameNameForDifferentUser() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(accountRepository.existsByUserIdAndBankNumberAndName(new UserId(1L), new BankNumber("007"), "Savings")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(command(AccountType.SAVINGS));

        assertThat(result.type()).isEqualTo(AccountType.SAVINGS);
    }

    @Test
    void defaultsAliasToCbuWhenAliasBlank() {
        when(bankRepository.findByBankNumber(any())).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(accountRepository.existsByUserIdAndBankNumberAndName(any(), any(), any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        OpenAccountCommand cmd = new OpenAccountCommand(
                new UserId(1L), new BankNumber("007"), "Sueldo", AccountType.CHECKING,
                new Money(BigDecimal.ZERO, Currency.getInstance("ARS")),
                true, "0070001600000000123459", "  ");

        Account saved = useCase.execute(cmd);

        assertThat(saved.alias()).isEqualTo("0070001600000000123459");
    }
}
