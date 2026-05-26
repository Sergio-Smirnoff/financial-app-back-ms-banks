package com.financialapp.banks.application.bank;

import com.financialapp.banks.application.bank.impl.ListBanksUseCaseImpl;
import com.financialapp.banks.application.bank.usecase.BankWithAccounts;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBanksUseCaseImplTest {

    @Mock AccountRepository accountRepository;

    private final UserId user = new UserId(1L);

    private ListBanksUseCaseImpl useCase() {
        return new ListBanksUseCaseImpl(accountRepository);
    }

    private Account account(BankName bank, String name) {
        LocalDateTime now = LocalDateTime.now();
        return new CheckingAccount("cbu-" + name, "alias",
                new Money(new BigDecimal("100.00"), Currency.getInstance("USD")),
                user, bank, name, true, now, now);
    }

    @Test
    void execute_groupsUserAccountsByBank_andDerivesBankFromEnum() {
        when(accountRepository.findByUserId(user)).thenReturn(List.of(
                account(BankName.GALICIA, "a1"),
                account(BankName.GALICIA, "a2"),
                account(BankName.SANTANDER, "a3")));

        List<BankWithAccounts> result = useCase().execute(user);

        assertThat(result).hasSize(2);
        BankWithAccounts galicia = result.stream()
                .filter(b -> b.bank().name() == BankName.GALICIA)
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
