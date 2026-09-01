package com.financialapp.banks.infrastructure.scheduler;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BalanceSnapshotRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceSnapshotSchedulerTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BalanceSnapshotRepository snapshotRepository;

    @InjectMocks
    private BalanceSnapshotScheduler scheduler;

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void captureDailySnapshots_perUserFailureIsolation() {
        UserId user1 = new UserId(1L);
        UserId user2 = new UserId(2L);

        when(accountRepository.findDistinctOwners()).thenReturn(List.of(user1, user2));
        when(accountRepository.findByUserId(user1)).thenThrow(new RuntimeException("DB error on user1"));

        Account account2 = Account.create(
                AccountType.SAVINGS,
                Cbu.from("0070001600000000123459"),
                "alias.user2",
                new Money(new BigDecimal("1000.00"), ARS),
                user2,
                new BankNumber("007"),
                "User2 Account",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(accountRepository.findByUserId(user2)).thenReturn(List.of(account2));
        when(cardRepository.findByUserId(user2)).thenReturn(List.of());
        when(loanRepository.findByUserId(user2)).thenReturn(List.of());

        scheduler.captureDailySnapshots();

        ArgumentCaptor<BalanceSnapshot> captor = ArgumentCaptor.forClass(BalanceSnapshot.class);
        verify(snapshotRepository, times(1)).save(captor.capture());

        BalanceSnapshot saved = captor.getValue();
        assertThat(saved.userId()).isEqualTo(user2);
        assertThat(saved.cashByCurrency()).hasSize(1);
        assertThat(saved.cashByCurrency().get(0).amount()).isEqualByComparingTo("1000.00");
    }
}
