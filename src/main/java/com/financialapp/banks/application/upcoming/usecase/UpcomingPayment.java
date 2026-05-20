package com.financialapp.banks.application.upcoming.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpcomingPayment(
    Long id,
    String type,
    String description,
    BigDecimal amount,
    String currency,
    LocalDate dueDate,
    int installmentNumber,
    int totalInstallments,
    boolean paid
) {}
