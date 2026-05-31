package com.financialapp.banks.domain.model.account.accountTypes;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.BankNumber;

import java.time.LocalDateTime;

public class SavingsAccount extends Account {

    public SavingsAccount(Cbu cbu, String alias, Money balance, UserId userId,
                          BankNumber bankNumber, String name, Boolean isActive,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(cbu, alias, balance, userId, bankNumber, name, isActive, createdAt, updatedAt);
    }

    @Override
    public Account withBalance(Money newBalance, LocalDateTime updatedAt) {
        return new SavingsAccount(cbu, alias, newBalance, userId, bankNumber, name, isActive, createdAt, updatedAt);
    }
}
