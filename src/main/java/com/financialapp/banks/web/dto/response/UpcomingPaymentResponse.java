package com.financialapp.banks.web.dto.response;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record UpcomingPaymentResponse(
    Long id,
    String type, // LOAN, CARD
    String description,
    String amount,
    String currency,
    LocalDate dueDate,
    int installmentNumber,
    int totalInstallments,
    boolean paid
) {}
