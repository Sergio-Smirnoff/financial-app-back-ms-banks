package com.financialapp.banks.model.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record UpcomingPaymentResponse(
    Long id,
    String type, // LOAN, CARD
    String description,
    BigDecimal amount,
    String currency,
    LocalDate dueDate,
    int installmentNumber,
    int totalInstallments,
    boolean paid
) {}
