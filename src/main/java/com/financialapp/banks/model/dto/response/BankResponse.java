package com.financialapp.banks.model.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BankResponse(
        Long id,
        Long userId,
        String name,
        String logoUrl,
        List<AccountResponse> accounts,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
