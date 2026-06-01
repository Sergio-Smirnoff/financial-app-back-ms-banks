package com.financialapp.banks.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Builder
public record CardResponse(
        String bankNumber,
        Long userId,
        String displayName,
        CardBrand brand,
        CardType cardType,
        CardBehavior behavior,
        String cardNumber,
        @JsonFormat(pattern = "MM/yy")
        @Schema(type = "string", example = "08/30", description = "Card expiry in MM/YY format")
        YearMonth expiringDate,
        int closingDay,
        int dueDay,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
