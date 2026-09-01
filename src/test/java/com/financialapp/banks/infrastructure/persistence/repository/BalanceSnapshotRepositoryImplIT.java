package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.domain.repository.BalanceSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BalanceSnapshotRepositoryImplIT {

    @Autowired
    private BalanceSnapshotRepository repository;

    @Test
    void roundTrip_allThreeMaps_persistsAndRestoresCorrectly() {
        UserId userId = new UserId(99L);
        LocalDate date = LocalDate.of(2026, 7, 30);
        Money cashArs = Money.of(new BigDecimal("150000.00"), "ARS");
        Money cashUsd = Money.of(new BigDecimal("1200.50"), "USD");
        Money cardDebt = Money.of(new BigDecimal("45000.00"), "ARS");
        Money loanDebt = Money.of(new BigDecimal("300000.00"), "ARS");

        BalanceSnapshot snapshot = BalanceSnapshot.create(
                userId, date, List.of(cashArs, cashUsd), List.of(cardDebt), List.of(loanDebt));

        BalanceSnapshot saved = repository.save(snapshot);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.userId()).isEqualTo(userId);
        assertThat(saved.snapshotDate()).isEqualTo(date);
        assertThat(saved.cashByCurrency()).containsExactlyInAnyOrder(cashArs, cashUsd);
        assertThat(saved.cardDebtByCurrency()).containsExactly(cardDebt);
        assertThat(saved.loanDebtByCurrency()).containsExactly(loanDebt);

        List<BalanceSnapshot> retrieved = repository.findByUserIdAndDateBetween(userId, date, date);
        assertThat(retrieved).hasSize(1);
        assertThat(retrieved.get(0).cashByCurrency()).containsExactlyInAnyOrder(cashArs, cashUsd);
    }

    @Test
    void save_sameDayTwice_upsertsSingleRowWithSecondValues() {
        UserId userId = new UserId(101L);
        LocalDate date = LocalDate.of(2026, 7, 30);

        Money cash1 = Money.of(new BigDecimal("100.00"), "ARS");
        BalanceSnapshot initial = BalanceSnapshot.create(userId, date, List.of(cash1), List.of(), List.of());
        repository.save(initial);

        Money cash2 = Money.of(new BigDecimal("250.00"), "ARS");
        BalanceSnapshot updated = BalanceSnapshot.create(userId, date, List.of(cash2), List.of(), List.of());
        repository.save(updated);

        List<BalanceSnapshot> retrieved = repository.findByUserIdAndDateBetween(userId, date, date);
        assertThat(retrieved).hasSize(1);
        assertThat(retrieved.get(0).cashByCurrency()).containsExactly(cash2);
    }

    @Test
    void findByUserIdAndDateBetween_multipleDatesAndUsers_filtersAndOrdersAscending() {
        UserId user1 = new UserId(200L);
        UserId user2 = new UserId(201L);

        LocalDate date1 = LocalDate.of(2026, 7, 1);
        LocalDate date2 = LocalDate.of(2026, 7, 15);
        LocalDate date3 = LocalDate.of(2026, 7, 30);

        repository.save(BalanceSnapshot.create(user1, date3, List.of(), List.of(), List.of()));
        repository.save(BalanceSnapshot.create(user1, date1, List.of(), List.of(), List.of()));
        repository.save(BalanceSnapshot.create(user1, date2, List.of(), List.of(), List.of()));
        repository.save(BalanceSnapshot.create(user2, date2, List.of(), List.of(), List.of()));

        List<BalanceSnapshot> results = repository.findByUserIdAndDateBetween(
                user1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 20));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).snapshotDate()).isEqualTo(date1);
        assertThat(results.get(1).snapshotDate()).isEqualTo(date2);
    }
}
