package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

import java.time.LocalDateTime;

public abstract class Account {
    String cbu; // id
    String alias;
    Money balance;
    UserId userId;
    BankName bankName;
    String name;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
