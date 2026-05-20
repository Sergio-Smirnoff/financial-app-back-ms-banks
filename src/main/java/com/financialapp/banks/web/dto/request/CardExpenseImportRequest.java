package com.financialapp.banks.web.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CardExpenseImportRequest(
    Long arsAccountId,
    Long usdAccountId,           // nullable — if null, USD expenses skipped
    List<ImportedExpense> expenses
) {
    public record ImportedExpense(
        String description,
        BigDecimal amount,
        String currency,          // "ARS" or "USD"
        LocalDate date
    ) {}
}
