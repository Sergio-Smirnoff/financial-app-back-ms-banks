package com.financialapp.banks.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanRequest(
        @NotNull Long accountId,
        @NotBlank String name,
        @NotNull @DecimalMin("0.01") BigDecimal principal,
        @NotNull @DecimalMin("0.00") BigDecimal interestRate,
        @Min(1) int totalInstallments,
        @NotNull LocalDate startDate
) {}
