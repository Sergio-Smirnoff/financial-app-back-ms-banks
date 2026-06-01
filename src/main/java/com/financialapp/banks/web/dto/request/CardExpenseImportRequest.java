package com.financialapp.banks.web.dto.request;

import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public record CardExpenseImportRequest(
    String arsAccountCbu,
    String usdAccountCbu,
    List<ImportedExpense> expenses
) {
    public record ImportedExpense(
        String description,
        @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "amount must be a non-negative decimal with up to 2 decimal places") String amount,
        String currency,
        LocalDate date
    ) {}
}
