package com.financialapp.banks.application.account;

import com.financialapp.banks.application.account.impl.UpdateAccountUseCaseImpl;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.account.AccountInvalidTypeException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.usecase.account.command.UpdateAccountCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAccountUseCaseImplTest {

    @Mock AccountRepository accountRepository;
    UpdateAccountUseCaseImpl useCase;

    private static final Cbu CBU = Cbu.from("0070001600000000123459");
    private static final BankNumber BANK = new BankNumber("007");
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 0, 0);

    @BeforeEach
    void setUp() {
        useCase = new UpdateAccountUseCaseImpl(accountRepository);
        lenientSave();
    }

    private void lenientSave() {
        lenient().when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CheckingAccount checking(String name) {
        return new CheckingAccount(CBU, "alias", new Money(new BigDecimal("100.00"), ARS),
                new UserId(1L), BANK, name, true, T0, T0);
    }

    private SavingsAccount savings(String name) {
        return new SavingsAccount(CBU, "alias", new Money(new BigDecimal("100.00"), ARS),
                new UserId(1L), BANK, name, true, T0, T0);
    }

    private InvestmentAccount investment(String name) {
        return new InvestmentAccount(CBU, "alias", new Money(new BigDecimal("100.00"), ARS),
                new UserId(1L), BANK, name, true, T0, T0);
    }

    @Test
    void throwsNotFound_whenAccountMissing() {
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(new UpdateAccountCommand(CBU.value(), "New", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void renamesCheckingAccount_whenNewNameFree() {
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.of(checking("Old")));
        when(accountRepository.existsByBankNumberAndName(BANK, "New")).thenReturn(false);

        Account result = useCase.execute(new UpdateAccountCommand(CBU.value(), "New", null, null));

        assertThat(result).isInstanceOf(CheckingAccount.class);
        assertThat(result.name()).isEqualTo("New");
    }

    @Test
    void rejectsRename_whenNewNameTakenInBank() {
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.of(checking("Old")));
        when(accountRepository.existsByBankNumberAndName(BANK, "Taken")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new UpdateAccountCommand(CBU.value(), "Taken", null, null)))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void keepsExistingFields_whenCommandFieldsNull_savingsAccount() {
        // name/balance/isActive all null -> all the "use existing" ternary branches
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.of(savings("Keep")));

        Account result = useCase.execute(new UpdateAccountCommand(CBU.value(), null, null, null));

        assertThat(result).isInstanceOf(SavingsAccount.class);
        assertThat(result.name()).isEqualTo("Keep");
        assertThat(result.balance().amount()).isEqualByComparingTo("100.00");
        assertThat(result.isActive()).isTrue();
        // name was null -> duplicate check skipped
        verify(accountRepository, never()).existsByBankNumberAndName(any(), any());
    }

    @Test
    void updatesBalanceAndActive_investmentAccount() {
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.of(investment("Same")));
        // name equal to existing -> duplicate check skipped (the !equals branch is false)
        Account result = useCase.execute(new UpdateAccountCommand(CBU.value(), "Same",
                new Money(new BigDecimal("250.00"), ARS), false));

        assertThat(result).isInstanceOf(InvestmentAccount.class);
        assertThat(result.balance().amount()).isEqualByComparingTo("250.00");
        assertThat(result.isActive()).isFalse();
        verify(accountRepository, never()).existsByBankNumberAndName(eq(BANK), eq("Same"));
    }

    @Test
    void throwsInvalidType_whenAccountSubtypeUnknown() {
        // A test-only Account subtype drives the defensive default branch of the switch.
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.of(new FakeAccount()));
        assertThatThrownBy(() -> useCase.execute(new UpdateAccountCommand(CBU.value(), "X", null, null)))
                .isInstanceOf(AccountInvalidTypeException.class);
    }

    /** Minimal non-Checking/Savings/Investment Account to exercise the unreachable default branch. */
    private static final class FakeAccount extends Account {
        FakeAccount() {
            super(CBU, "alias", new Money(new BigDecimal("100.00"), ARS), new UserId(1L), BANK, "Fake", true, T0, T0);
        }
        @Override
        public Account withBalance(Money newBalance, LocalDateTime updatedAt) {
            return this;
        }
    }
}
