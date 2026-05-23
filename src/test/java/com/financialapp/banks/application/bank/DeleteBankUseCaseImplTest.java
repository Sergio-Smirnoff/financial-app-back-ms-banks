package com.financialapp.banks.application.bank;

import com.financialapp.banks.application.bank.command.DeleteBankCommand;
import com.financialapp.banks.application.bank.impl.DeleteBankUseCaseImpl;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
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
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteBankUseCaseImplTest {

    @Mock BankRepository bankRepository;
    @Mock AccountRepository accountRepository;
    DeleteBankUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteBankUseCaseImpl(bankRepository, accountRepository);
    }

    @Test
    void delete_removesBankWithNoActiveAccounts() {
        Bank bank = new Bank(BankName.GALICIA, null);
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.of(bank));
        when(accountRepository.findByBankName(BankName.GALICIA)).thenReturn(List.of());

        useCase.execute(new DeleteBankCommand(BankName.GALICIA));

        verify(bankRepository).delete(BankName.GALICIA);
    }

    @Test
    void delete_rejectsWhenActiveAccountsExist() {
        Bank bank = new Bank(BankName.GALICIA, null);
        Account account = new CheckingAccount(
                "1234567890123456789012", "alias",
                new Money(BigDecimal.TEN, Currency.getInstance("USD")),
                new UserId(1L), BankName.GALICIA, "My acc", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.of(bank));
        when(accountRepository.findByBankName(BankName.GALICIA)).thenReturn(List.of(account));

        assertThatThrownBy(() -> useCase.execute(new DeleteBankCommand(BankName.GALICIA)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete bank");
    }

    @Test
    void delete_throwsWhenBankMissing() {
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeleteBankCommand(BankName.GALICIA)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
