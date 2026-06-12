package com.financialapp.banks.application.bank;

import com.financialapp.banks.application.bank.impl.ListBanksUseCaseImpl;
import com.financialapp.banks.domain.usecase.bank.BankWithAccounts;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.bank.Logo;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBanksUseCaseImplTest {

    @Mock AccountRepository accountRepository;
    @Mock BankRepository bankRepository;

    private final UserId user = new UserId(1L);
    private static final Cbu A_CBU = Cbu.from("0070001600000000123459");

    private ListBanksUseCaseImpl useCase() {
        return new ListBanksUseCaseImpl(accountRepository, bankRepository);
    }

    private Account account(BankNumber bank, String name) {
        LocalDateTime now = LocalDateTime.now();
        return new Account(AccountType.CHECKING, A_CBU, "alias",
                new Money(new BigDecimal("100.00"), Currency.getInstance("USD")),
                user, bank, name, true, now, now);
    }

    @Test
    void execute_groupsUserAccountsByBank_andResolvesBankFromCatalog() {
        when(accountRepository.findByUserId(user)).thenReturn(List.of(
                account(new BankNumber("007"), "a1"),
                account(new BankNumber("007"), "a2"),
                account(new BankNumber("072"), "a3")));
        when(bankRepository.findByBankNumber(new BankNumber("007")))
                .thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", new Logo(null))));
        when(bankRepository.findByBankNumber(new BankNumber("072")))
                .thenReturn(Optional.of(new Bank(new BankNumber("072"), "SANTANDER", new Logo(null))));

        List<BankWithAccounts> result = useCase().execute(user);

        assertThat(result).hasSize(2);
        BankWithAccounts galicia = result.stream()
                .filter(b -> b.bank().bankNumber().equals(new BankNumber("007")))
                .findFirst().orElseThrow();
        assertThat(galicia.accounts()).hasSize(2);
        assertThat(galicia.bank().logo().url()).isNull();
    }

    @Test
    void execute_noAccounts_returnsEmpty() {
        when(accountRepository.findByUserId(user)).thenReturn(List.of());

        assertThat(useCase().execute(user)).isEmpty();
    }
}
