package com.financialapp.banks.application.account;

import com.financialapp.banks.domain.usecase.account.command.OpenAccountCommand;
import com.financialapp.banks.application.account.impl.OpenAccountUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
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
                new UserId(1L), BankName.GALICIA, "Savings", type,
                new Money(new BigDecimal("100.00"), Currency.getInstance("USD")),
                true, "1234567890123456789012", "alias");
    }

    @Test
    void create_persistsSavingsAccount() {
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.of(new Bank(BankName.GALICIA, null)));
        when(accountRepository.existsByBankNameAndName(BankName.GALICIA, "Savings")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(command(AccountType.SAVINGS));

        assertThat(result).isInstanceOf(SavingsAccount.class);
        assertThat(result.balance().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void create_rejectsDuplicateName() {
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.of(new Bank(BankName.GALICIA, null)));
        when(accountRepository.existsByBankNameAndName(BankName.GALICIA, "Savings")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command(AccountType.SAVINGS)))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }
}
