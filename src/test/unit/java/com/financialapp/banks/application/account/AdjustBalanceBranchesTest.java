package com.financialapp.banks.application.account;

import com.financialapp.banks.application.account.impl.AdjustBalanceUseCaseImpl;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Branch coverage for AdjustBalance: currency-less delta normalization and the negative (debit) path. */
@ExtendWith(MockitoExtension.class)
class AdjustBalanceBranchesTest {

    @Mock AccountRepository accountRepository;
    @Mock DomainEventPublisher eventPublisher;
    AdjustBalanceUseCaseImpl useCase;

    private static final Currency USD = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        useCase = new AdjustBalanceUseCaseImpl(accountRepository, eventPublisher);
    }

    private CheckingAccount checking(String balance) {
        return new CheckingAccount(Cbu.from("0070001600000000123459"), "alias",
                new Money(new BigDecimal(balance), USD), new UserId(1L), new BankNumber("007"),
                "acc", true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void normalizesCurrencyLessDeltaToAccountCurrency() {
        // Given a delta with no currency (currency() == null branch)
        CheckingAccount acc = checking("100.00");
        when(accountRepository.findByCbu(acc.cbu().value())).thenReturn(Optional.of(acc));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new AdjustBalanceCommand(acc.cbu().value(), new Money(new BigDecimal("50.00"), null)));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().balance().amount()).isEqualByComparingTo("150.00");
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void debitsBalanceAndPublishesEvents_whenDeltaNegative() {
        // Given a negative delta with sufficient funds (amount.isNegative() == true branch)
        CheckingAccount acc = checking("100.00");
        when(accountRepository.findByCbu(acc.cbu().value())).thenReturn(Optional.of(acc));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new AdjustBalanceCommand(acc.cbu().value(), new Money(new BigDecimal("-30.00"), USD)));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().balance().amount()).isEqualByComparingTo("70.00");
        verify(eventPublisher).publishAll(anyList());
    }
}
