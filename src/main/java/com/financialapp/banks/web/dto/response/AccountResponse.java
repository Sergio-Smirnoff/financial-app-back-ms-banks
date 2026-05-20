package com.financialapp.banks.model.dto.response;

import com.financialapp.banks.model.enums.AccountType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountResponse(
        Long id,
        Long bankId,
        Long userId,
        String name,
        AccountType type,
        BigDecimal balance,
        String currency,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public AccountResponse withBalance(BigDecimal newBalance) {
        return new AccountResponse(id, bankId, userId, name, type, newBalance, currency, isActive, createdAt, updatedAt);
    }
}
