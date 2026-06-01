package com.financialapp.banks.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CardExpenseCreateRequest(
        @NotBlank String description,
        @NotNull @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "totalAmount must be a non-negative decimal with up to 2 decimal places") String totalAmount,
        @NotBlank String currency,
        @Min(1) int totalInstallments,
        @NotNull LocalDate firstDueDate
) {}
