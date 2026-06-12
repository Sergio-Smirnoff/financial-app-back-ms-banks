package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.domain.exception.account.AccountInsufficientFundsException;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Account {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500.00");

    protected final Cbu cbu;
    protected final String alias;
    protected final Money balance;
    protected final UserId userId;
    protected final BankNumber bankNumber;
    protected final String name;
    protected final Boolean isActive;
    protected final LocalDateTime createdAt;
    protected final LocalDateTime updatedAt;

    protected Account(Cbu cbu, String alias, Money balance, UserId userId,
                      BankNumber bankNumber, String name, Boolean isActive,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cbu = cbu;
        this.alias = alias;
        this.balance = balance;
        this.userId = userId;
        this.bankNumber = bankNumber;
        this.name = name;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account create(AccountType type, Cbu cbu, String alias, Money balance,
                                 UserId userId, BankNumber bankNumber, String name, boolean isActive,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return switch (type) {
            case CHECKING -> new CheckingAccount(cbu, alias, balance, userId, bankNumber, name, isActive, createdAt, updatedAt);
            case SAVINGS -> new SavingsAccount(cbu, alias, balance, userId, bankNumber, name, isActive, createdAt, updatedAt);
        };
    }

    public Cbu cbu() { return cbu; }
    public String alias() { return alias; }
    public Money balance() { return balance; }
    public UserId userId() { return userId; }
    public BankNumber bankNumber() { return bankNumber; }
    public String name() { return name; }
    public Boolean isActive() { return isActive; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }

    public abstract Account withBalance(Money newBalance, LocalDateTime updatedAt);

    public AccountAdjustment debit(Money amount, LocalDateTime when) {
        if (balance.isLessThan(amount)) {
            throw new AccountInsufficientFundsException(cbu.value(), balance, amount);
        }
        Account adjusted = withBalance(balance.subtract(amount), when);
        return adjusted.adjustmentWith(new Money(amount.amount().negate(), amount.currency()));
    }

    public AccountAdjustment credit(Money amount, LocalDateTime when) {
        Account adjusted = withBalance(balance.add(amount), when);
        return adjusted.adjustmentWith(amount);
    }

    private AccountAdjustment adjustmentWith(Money signedDelta) {
        List<DomainEvent> events = new ArrayList<>();
        events.add(new BalanceAdjustedEvent(userId, cbu.value(), bankNumber, name, signedDelta));
        if (isLowBalance(new Money(LOW_BALANCE_THRESHOLD, balance.currency()))) {
            events.add(new LowBalanceEvent(userId, cbu.value(), bankNumber, name, balance));
        }
        return new AccountAdjustment(this, events);
    }

    public boolean isLowBalance(Money threshold) {
        return balance.isLessThan(threshold);
    }
}
