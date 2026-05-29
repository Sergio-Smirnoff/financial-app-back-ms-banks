package com.financialapp.banks.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record LoanRequest(
        @NotBlank String bankName,
        @NotBlank String destinationAccountCbu,
        @NotBlank String name,
        @NotNull @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "principal must be a non-negative decimal with up to 2 decimal places") String principal,
        @NotNull @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "interestRate must be a non-negative decimal") String interestRate,
        @Min(1) int totalInstallments,
        @NotNull LocalDate startDate
) {}
