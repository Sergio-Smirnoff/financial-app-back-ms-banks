package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CardInstallment(
    CardInstallmentId id,
    String cardNumber,
    String description,
    Money totalAmount,
    int installmentNumber,
    int totalInstallments,
    Money amount,
    LocalDate dueDate,
    boolean paid,
    LocalDate paidDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
