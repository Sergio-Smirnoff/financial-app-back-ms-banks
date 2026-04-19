package com.financialapp.banks.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardExpenseCreateRequest(
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal totalAmount,
        @NotBlank String currency,
        @Min(1) int totalInstallments,
        @NotNull LocalDate firstDueDate
) {}
