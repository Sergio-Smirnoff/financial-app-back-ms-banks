package com.financialapp.banks.domain.query;

import com.financialapp.banks.domain.common.model.Money;

import java.time.LocalDate;

/** Read-model row for the upcoming-payments projection. Not an aggregate. */
public record UpcomingInstallment(
        Long installmentId,
        String type,            // "LOAN" or "CARD"
        String description,
        Money amount,
        LocalDate dueDate,
        int installmentNumber,
        int totalInstallments,
        boolean paid
) {}
