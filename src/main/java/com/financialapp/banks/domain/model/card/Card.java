package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

import java.time.LocalDateTime;

public record Card(
    CardId id,
    UserId userId,
    BankName bankName,
    CardDetails details,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
