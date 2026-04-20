package com.financialapp.banks.model.dto.response;

import com.financialapp.banks.model.enums.CardBehavior;
import com.financialapp.banks.model.enums.CardBrand;
import com.financialapp.banks.model.enums.CardType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CardResponse(
        Long id,
        Long bankId,
        Long userId,
        String displayName,
        CardBrand brand,
        CardType cardType,
        CardBehavior behavior,
        String last4Digits,
        LocalDate expiringDate,
        int closingDay,
        int dueDay,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
