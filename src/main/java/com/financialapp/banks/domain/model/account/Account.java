package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.domain.exception.account.AccountInsufficientFundsException;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.model.bank.BankName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Account {

    /** Balance below which a {@link LowBalanceEvent} is raised (in the account's own currency). */
    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500.00");

    protected final String cbu;
    protected final String alias;
    protected final Money balance;
    protected final UserId userId;
    protected final BankName bankName;
    protected final String name;
    protected final Boolean isActive;
    protected final LocalDateTime createdAt;
    protected final LocalDateTime updatedAt;

    protected Account(String cbu, String alias, Money balance, UserId userId,
                      BankName bankName, String name, Boolean isActive,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cbu = cbu;
        this.alias = alias;
        this.balance = balance;
        this.userId = userId;
        this.bankName = bankName;
        this.name = name;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account create(AccountType type, String cbu, String alias, Money balance,
                                 UserId userId, BankName bankName, String name, boolean isActive,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return switch (type) {
            case CHECKING -> new CheckingAccount(cbu, alias, balance, userId, bankName, name, isActive, createdAt, updatedAt);
            case SAVINGS -> new SavingsAccount(cbu, alias, balance, userId, bankName, name, isActive, createdAt, updatedAt);
            case INVESTMENT -> new InvestmentAccount(cbu, alias, balance, userId, bankName, name, isActive, createdAt, updatedAt);
        };
    }

    public String cbu() { return cbu; }
    public String alias() { return alias; }
    public Money balance() { return balance; }
    public UserId userId() { return userId; }
    public BankName bankName() { return bankName; }
    public String name() { return name; }
    public Boolean isActive() { return isActive; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }

    public abstract Account withBalance(Money newBalance, LocalDateTime updatedAt);

    /**
     * Removes {@code amount} from the balance. The amount is a positive magnitude.
     * Enforces the investment-account restriction, the same-currency guard
     * (via {@link Money#subtract}) and the no-overdraft (insufficient funds) invariant.
     * Records a {@link BalanceAdjustedEvent} (signed delta = {@code -amount}) and, when the
     * new balance is low, a {@link LowBalanceEvent}.
     */
    public AccountAdjustment debit(Money amount, LocalDateTime when) {
        ensureNotInvestmentRestricted();
        if (balance.isLessThan(amount)) {
            throw new AccountInsufficientFundsException(cbu, balance, amount);
        }
        Account adjusted = withBalance(balance.subtract(amount), when);
        return adjusted.adjustmentWith(new Money(amount.amount().negate(), amount.currency()));
    }

    /**
     * Adds {@code amount} to the balance. The amount is a positive magnitude.
     * Enforces the investment-account restriction and the same-currency guard
     * (via {@link Money#add}). Records a {@link BalanceAdjustedEvent} (signed delta = {@code +amount})
     * and, when the resulting balance is low, a {@link LowBalanceEvent}.
     */
    public AccountAdjustment credit(Money amount, LocalDateTime when) {
        ensureNotInvestmentRestricted();
        Account adjusted = withBalance(balance.add(amount), when);
        return adjusted.adjustmentWith(amount);
    }

    /** Builds the events for a just-applied balance change on this (already-updated) account. */
    private AccountAdjustment adjustmentWith(Money signedDelta) {
        List<DomainEvent> events = new ArrayList<>();
        events.add(new BalanceAdjustedEvent(userId, cbu, bankName, name, signedDelta));
        if (isLowBalance(new Money(LOW_BALANCE_THRESHOLD, balance.currency()))) {
            events.add(new LowBalanceEvent(userId, cbu, bankName, name, balance));
        }
        return new AccountAdjustment(this, events);
    }

    public boolean isLowBalance(Money threshold) {
        return balance.isLessThan(threshold);
    }

    /**
     * Hook for account types that forbid manual balance adjustments.
     * Default: no restriction.
     */
    protected void ensureNotInvestmentRestricted() {
        // no-op by default
    }
}
