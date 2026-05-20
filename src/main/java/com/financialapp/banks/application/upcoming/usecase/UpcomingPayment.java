package com.financialapp.banks.application.upcoming.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.financialapp.banks.domain.common.model.Money;

public record UpcomingPayment(
    Long id,
    String type,
    String description,
    Money amount,
    LocalDate dueDate,
    int installmentNumber,
    int totalInstallments,
    boolean paid
) {}
