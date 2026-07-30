package com.financialapp.banks.domain.model.snapshot;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.snapshot.InvalidBalanceSnapshotException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class BalanceSnapshot {

    private final BalanceSnapshotId id;
    private final UserId userId;
    private final LocalDate snapshotDate;
    private final List<Money> cashByCurrency;
    private final List<Money> cardDebtByCurrency;
    private final List<Money> loanDebtByCurrency;
    private final LocalDateTime createdAt;

    public BalanceSnapshot(BalanceSnapshotId id,
                           UserId userId,
                           LocalDate snapshotDate,
                           List<Money> cashByCurrency,
                           List<Money> cardDebtByCurrency,
                           List<Money> loanDebtByCurrency,
                           LocalDateTime createdAt) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.snapshotDate = Objects.requireNonNull(snapshotDate, "snapshotDate must not be null");
        this.cashByCurrency = validateAndCopy(cashByCurrency, "cashByCurrency");
        this.cardDebtByCurrency = validateAndCopy(cardDebtByCurrency, "cardDebtByCurrency");
        this.loanDebtByCurrency = validateAndCopy(loanDebtByCurrency, "loanDebtByCurrency");
        this.createdAt = Objects.requireNonNullElseGet(createdAt, LocalDateTime::now);
    }

    public static BalanceSnapshot create(UserId userId,
                                        LocalDate snapshotDate,
                                        List<Money> cashByCurrency,
                                        List<Money> cardDebtByCurrency,
                                        List<Money> loanDebtByCurrency) {
        return new BalanceSnapshot(null, userId, snapshotDate, cashByCurrency, cardDebtByCurrency, loanDebtByCurrency, LocalDateTime.now());
    }

    public static BalanceSnapshot reconstitute(BalanceSnapshotId id,
                                              UserId userId,
                                              LocalDate snapshotDate,
                                              List<Money> cashByCurrency,
                                              List<Money> cardDebtByCurrency,
                                              List<Money> loanDebtByCurrency,
                                              LocalDateTime createdAt) {
        return new BalanceSnapshot(id, userId, snapshotDate, cashByCurrency, cardDebtByCurrency, loanDebtByCurrency, createdAt);
    }

    private static List<Money> validateAndCopy(List<Money> source, String fieldName) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        Set<Currency> seen = new HashSet<>();
        for (Money money : source) {
            if (money == null || money.currency() == null) {
                throw new InvalidBalanceSnapshotException(fieldName + " contains null Money or Currency");
            }
            if (!seen.add(money.currency())) {
                throw new InvalidBalanceSnapshotException(
                        "Duplicate currency '" + money.currency().getCurrencyCode() + "' in " + fieldName);
            }
        }
        return List.copyOf(source);
    }

    public BalanceSnapshotId id() { return id; }
    public UserId userId() { return userId; }
    public LocalDate snapshotDate() { return snapshotDate; }
    public List<Money> cashByCurrency() { return cashByCurrency; }
    public List<Money> cardDebtByCurrency() { return cardDebtByCurrency; }
    public List<Money> loanDebtByCurrency() { return loanDebtByCurrency; }
    public LocalDateTime createdAt() { return createdAt; }
}
