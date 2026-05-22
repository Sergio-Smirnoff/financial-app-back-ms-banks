package com.financialapp.banks.domain.model.account.accountTypes;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankName;

import java.time.LocalDateTime;

public class SavingsAccount extends Account {

    public SavingsAccount(String cbu, String alias, Money balance, UserId userId,
                          BankName bankName, String name, Boolean isActive,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(cbu, alias, balance, userId, bankName, name, isActive, createdAt, updatedAt);
    }

    @Override
    public AccountType type() { return AccountType.SAVINGS; }
}
