package com.financialapp.banks.model.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record BankResponse(
        Long id,
        Long userId,
        String name,
        String logoUrl,
        List<AccountResponse> accounts,
        Map<String, BigDecimal> totalBalances,
        int accountsCount,
        int cardsCount,
        int loansCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
