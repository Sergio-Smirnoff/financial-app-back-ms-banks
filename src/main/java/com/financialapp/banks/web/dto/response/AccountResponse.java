package com.financialapp.banks.web.dto.response;

import com.financialapp.banks.domain.model.account.AccountType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountResponse(
        String bankName,
        Long userId,
        String name,
        AccountType type,
        BigDecimal balance,
        String currency,
        String cbu,
        String alias,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
