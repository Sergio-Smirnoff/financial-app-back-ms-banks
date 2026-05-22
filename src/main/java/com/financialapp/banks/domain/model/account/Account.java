package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

import java.time.LocalDateTime;

public abstract class Account {

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

    public String cbu() { return cbu; }
    public String alias() { return alias; }
    public Money balance() { return balance; }
    public UserId userId() { return userId; }
    public BankName bankName() { return bankName; }
    public String name() { return name; }
    public Boolean isActive() { return isActive; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }

    public abstract AccountType type();
}
