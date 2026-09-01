package com.financialapp.banks.domain.model.snapshot;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.snapshot.InvalidBalanceSnapshotException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceSnapshotTest {

    @Test
    void create_validSnapshot_constructsCorrectly() {
        UserId userId = new UserId(1L);
        LocalDate date = LocalDate.of(2026, 7, 30);
        Money cashArs = Money.of(new BigDecimal("1000.00"), "ARS");
        Money cashUsd = Money.of(new BigDecimal("500.00"), "USD");

        BalanceSnapshot snapshot = BalanceSnapshot.create(
                userId, date, List.of(cashArs, cashUsd), List.of(), List.of());

        assertThat(snapshot.id()).isNull();
        assertThat(snapshot.userId()).isEqualTo(userId);
        assertThat(snapshot.snapshotDate()).isEqualTo(date);
        assertThat(snapshot.cashByCurrency()).containsExactly(cashArs, cashUsd);
        assertThat(snapshot.cardDebtByCurrency()).isEmpty();
        assertThat(snapshot.loanDebtByCurrency()).isEmpty();
        assertThat(snapshot.createdAt()).isNotNull();
    }

    @Test
    void create_nullUserId_throwsException() {
        LocalDate date = LocalDate.of(2026, 7, 30);
        assertThatThrownBy(() -> BalanceSnapshot.create(null, date, List.of(), List.of(), List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void create_nullSnapshotDate_throwsException() {
        UserId userId = new UserId(1L);
        assertThatThrownBy(() -> BalanceSnapshot.create(userId, null, List.of(), List.of(), List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void create_nullLists_defaultsToEmptyLists() {
        UserId userId = new UserId(1L);
        LocalDate date = LocalDate.of(2026, 7, 30);

        BalanceSnapshot snapshot = BalanceSnapshot.create(userId, date, null, null, null);

        assertThat(snapshot.cashByCurrency()).isEmpty();
        assertThat(snapshot.cardDebtByCurrency()).isEmpty();
        assertThat(snapshot.loanDebtByCurrency()).isEmpty();
    }

    @Test
    void create_duplicateCurrencyInList_throwsInvalidBalanceSnapshotException() {
        UserId userId = new UserId(1L);
        LocalDate date = LocalDate.of(2026, 7, 30);
        Money cash1 = Money.of(new BigDecimal("1000.00"), "ARS");
        Money cash2 = Money.of(new BigDecimal("500.00"), "ARS");

        assertThatThrownBy(() -> BalanceSnapshot.create(userId, date, List.of(cash1, cash2), List.of(), List.of()))
                .isInstanceOf(InvalidBalanceSnapshotException.class);
    }

    @Test
    void listCopiesAreDefensive() {
        UserId userId = new UserId(1L);
        LocalDate date = LocalDate.of(2026, 7, 30);
        Money cash = Money.of(new BigDecimal("1000.00"), "ARS");

        BalanceSnapshot snapshot = BalanceSnapshot.create(userId, date, List.of(cash), List.of(), List.of());

        assertThatThrownBy(() -> snapshot.cashByCurrency().add(Money.of(new BigDecimal("100.00"), "USD")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void reconstitute_withAllFields_setsProperties() {
        BalanceSnapshotId id = new BalanceSnapshotId(42L);
        UserId userId = new UserId(1L);
        LocalDate date = LocalDate.of(2026, 7, 30);
        LocalDateTime now = LocalDateTime.now();
        Money cash = Money.of(new BigDecimal("1000.00"), "ARS");

        BalanceSnapshot snapshot = BalanceSnapshot.reconstitute(id, userId, date, List.of(cash), List.of(), List.of(), now);

        assertThat(snapshot.id()).isEqualTo(id);
        assertThat(snapshot.userId()).isEqualTo(userId);
        assertThat(snapshot.snapshotDate()).isEqualTo(date);
        assertThat(snapshot.cashByCurrency()).containsExactly(cash);
        assertThat(snapshot.createdAt()).isEqualTo(now);
    }
}
