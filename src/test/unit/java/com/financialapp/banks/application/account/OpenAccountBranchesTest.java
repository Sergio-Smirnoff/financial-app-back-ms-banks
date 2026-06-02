package com.financialapp.banks.application.account;

import com.financialapp.banks.application.account.impl.OpenAccountUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.cbu.CbuBankMismatchException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.usecase.account.command.OpenAccountCommand;
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

/** Branch coverage for OpenAccount: bank-missing, CBU/bank mismatch, investment-currency dup, isActive default. */
@ExtendWith(MockitoExtension.class)
class OpenAccountBranchesTest {

    @Mock BankRepository bankRepository;
    @Mock AccountRepository accountRepository;
    OpenAccountUseCaseImpl useCase;

    private static final Currency USD = Currency.getInstance("USD");
    private static final String CBU_BANK_007 = "0070001600000000123459";

    @BeforeEach
    void setUp() {
        useCase = new OpenAccountUseCaseImpl(accountRepository, bankRepository);
    }

    private OpenAccountCommand command(BankNumber bank, AccountType type, Boolean isActive) {
        return new OpenAccountCommand(new UserId(1L), bank, "Acc", type,
                new Money(new BigDecimal("100.00"), USD), isActive, CBU_BANK_007, "alias");
    }

    @Test
    void throwsNotFound_whenBankMissing() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(command(new BankNumber("007"), AccountType.CHECKING, true)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void throwsMismatch_whenCbuBankDiffersFromCommandBank() {
        // Bank 014 exists, but the CBU belongs to bank 007 -> mismatch
        when(bankRepository.findByBankNumber(new BankNumber("014")))
                .thenReturn(Optional.of(new Bank(new BankNumber("014"), "X", null)));
        assertThatThrownBy(() -> useCase.execute(command(new BankNumber("014"), AccountType.CHECKING, true)))
                .isInstanceOf(CbuBankMismatchException.class);
    }

    @Test
    void rejectsInvestment_whenSameCurrencyAlreadyExists() {
        when(bankRepository.findByBankNumber(new BankNumber("007")))
                .thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(accountRepository.existsByBankNumberAndName(new BankNumber("007"), "Acc")).thenReturn(false);
        when(accountRepository.existsByBankNumberAndTypeAndCurrency(new BankNumber("007"),
                AccountType.INVESTMENT.name(), USD)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command(new BankNumber("007"), AccountType.INVESTMENT, true)))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void createsInvestment_andDefaultsIsActive_whenNull() {
        when(bankRepository.findByBankNumber(new BankNumber("007")))
                .thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(accountRepository.existsByBankNumberAndName(new BankNumber("007"), "Acc")).thenReturn(false);
        when(accountRepository.existsByBankNumberAndTypeAndCurrency(new BankNumber("007"),
                AccountType.INVESTMENT.name(), USD)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(command(new BankNumber("007"), AccountType.INVESTMENT, null));

        assertThat(result).isInstanceOf(InvestmentAccount.class);
        assertThat(result.isActive()).isTrue();
    }
}
