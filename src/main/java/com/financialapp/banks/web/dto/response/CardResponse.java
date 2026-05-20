package com.financialapp.banks.web.dto.response;

import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CardResponse(
        Long id,
        String bankName,
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
