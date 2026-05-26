package com.financialapp.banks.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.YearMonth;

public record CardRequest(
        @NotBlank String bankName,
        @NotNull CardBrand brand,
        @NotNull CardType cardType,
        @NotNull CardBehavior behavior,
        @NotBlank @Pattern(regexp = "^\\d{16}$", message = "Must be exactly 16 digits") String cardNumber,
        @NotNull
        @JsonFormat(pattern = "MM/yy")
        @Schema(type = "string", example = "08/30", description = "Card expiry in MM/YY format")
        YearMonth expiringDate,
        @Min(1) @Max(31) int closingDay,
        @Min(1) @Max(31) int dueDay
) {}
