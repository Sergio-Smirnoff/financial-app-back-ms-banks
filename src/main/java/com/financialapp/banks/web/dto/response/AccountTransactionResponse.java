package com.financialapp.banks.web.dto.response;

import java.time.LocalDate;

public record AccountTransactionResponse(
        Long transactionId,
        String accountCbu,
        String amount,
        String currency,
        String description,
        String category,
        String subcategory,
        LocalDate date
) {}
