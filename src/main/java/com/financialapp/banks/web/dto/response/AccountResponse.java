package com.financialapp.banks.web.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountResponse(
        String bankName,
        Long userId,
        String name,
        String type,
        BigDecimal balance,
        String currency,
        String cbu,
        String alias,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
