package com.financialapp.banks.web.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CardExpenseImportRequest(
    String arsAccountCbu,
    String usdAccountCbu,
    List<ImportedExpense> expenses
) {
    public record ImportedExpense(
        String description,
        BigDecimal amount,
        String currency,
        LocalDate date
    ) {}
}
