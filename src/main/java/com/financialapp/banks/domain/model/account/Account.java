package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

import java.time.LocalDateTime;

public record Account(
    String cbu, 
    String alias,
    Money balance,
    UserId userId,
    BankName bankName,
    AccountDetails details,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
