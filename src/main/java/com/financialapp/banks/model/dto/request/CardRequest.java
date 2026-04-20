package com.financialapp.banks.model.dto.request;

import com.financialapp.banks.model.enums.CardBehavior;
import com.financialapp.banks.model.enums.CardBrand;
import com.financialapp.banks.model.enums.CardType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CardRequest(
        @NotNull Long bankId,
        @NotNull CardBrand brand,
        @NotNull CardType cardType,
        @NotNull CardBehavior behavior,
        @NotBlank @Pattern(regexp = "^\\d{4}$", message = "Must be exactly 4 digits") String last4Digits,
        @NotNull LocalDate expiringDate,
        @Min(1) @Max(31) int closingDay,
        @Min(1) @Max(31) int dueDay
) {}
