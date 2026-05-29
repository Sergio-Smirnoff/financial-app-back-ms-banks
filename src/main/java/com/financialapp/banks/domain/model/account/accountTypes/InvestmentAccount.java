package com.financialapp.banks.domain.model.account.accountTypes;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.account.AccountInvestmentRestrictionException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.BankName;

import java.time.LocalDateTime;

public class InvestmentAccount extends Account {

    public InvestmentAccount(String cbu, String alias, Money balance, UserId userId,
                             BankName bankName, String name, Boolean isActive,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(cbu, alias, balance, userId, bankName, name, isActive, createdAt, updatedAt);
    }

    @Override
    public Account withBalance(Money newBalance, LocalDateTime updatedAt) {
        return new InvestmentAccount(cbu, alias, newBalance, userId, bankName, name, isActive, createdAt, updatedAt);
    }

    @Override
    protected void ensureNotInvestmentRestricted() {
        throw new AccountInvestmentRestrictionException(cbu);
    }
}
