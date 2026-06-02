package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountAggregatePersistenceIT {

    @Autowired AccountRepository accountRepository;

    private static final String CBU = "0070001600000000123459";
    private static final BankNumber BANK = new BankNumber("007");
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 0, 0);

    private Account checking(String balance, String name) {
        return new CheckingAccount(Cbu.from(CBU), "alias", new Money(new BigDecimal(balance), ARS),
                new UserId(1L), BANK, name, true, T0, T0);
    }

    @Test
    void save_then_findByCbu_roundTrips() {
        // Given a new account / When saved and reloaded by CBU
        accountRepository.save(checking("100.00", "Main"));
        Account reloaded = accountRepository.findByCbu(CBU).orElseThrow();

        // Then its fields survive the mapper round-trip
        assertThat(reloaded.name()).isEqualTo("Main");
        assertThat(reloaded.balance().amount()).isEqualByComparingTo("100.00");
        assertThat(reloaded.bankNumber()).isEqualTo(BANK);
    }

    @Test
    void save_existingCbu_mergesInPlace() {
        // Given an already-persisted account / When saved again with a new balance (merge path)
        accountRepository.save(checking("100.00", "Main"));
        accountRepository.save(checking("999.00", "Main"));

        // Then the balance reflects the update
        assertThat(accountRepository.findByCbu(CBU).orElseThrow().balance().amount()).isEqualByComparingTo("999.00");
    }

    @Test
    void readQueries_returnPersistedAccount() {
        // Given a low-balance account
        accountRepository.save(checking("100.00", "Main"));

        // When / Then the various read projections find it
        assertThat(accountRepository.findByUserId(new UserId(1L))).isNotEmpty();
        assertThat(accountRepository.findByBankNumber(BANK)).isNotEmpty();
        assertThat(accountRepository.countByBankNumber(BANK)).isPositive();
        assertThat(accountRepository.findByAliasAndBankNumber("alias", BANK)).isPresent();
        assertThat(accountRepository.existsByBankNumberAndName(BANK, "Main")).isTrue();
        assertThat(accountRepository.existsByBankNumberAndTypeAndCurrency(BANK, "CHECKING", ARS)).isTrue();
        assertThat(accountRepository.findLowBalance(new BigDecimal("500.00"))).isNotEmpty();
        // findFiltered with all filters present
        assertThat(accountRepository.findFiltered(new UserId(1L), "CHECKING", ARS, BANK, "Main", true)).isNotEmpty();
        // findFiltered with the optional filters null (the other branch of each ternary)
        assertThat(accountRepository.findFiltered(new UserId(1L), null, null, null, null, false)).isNotEmpty();
    }

    @Test
    void delete_removesAccount() {
        // Given a persisted account / When deleted
        accountRepository.save(checking("100.00", "Main"));
        accountRepository.delete(CBU);

        // Then it can no longer be found
        assertThat(accountRepository.findByCbu(CBU)).isEmpty();
    }

    @Test
    void save_throwsWhenBankMissing() {
        // Given an account referencing a non-seeded bank / When saved / Then requireBank rejects it
        Account orphan = new CheckingAccount(Cbu.from(CBU), "alias", new Money(new BigDecimal("100.00"), ARS),
                new UserId(1L), new BankNumber("999"), "Orphan", true, T0, T0);
        assertThatThrownBy(() -> accountRepository.save(orphan)).isInstanceOf(ResourceNotFoundException.class);
    }
}
