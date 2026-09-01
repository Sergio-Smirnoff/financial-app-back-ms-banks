package com.financialapp.banks.domain.model.card;

import java.time.LocalDate;

public record BillingPeriod(LocalDate closingDate, LocalDate dueDate, boolean statementOpen) {}
