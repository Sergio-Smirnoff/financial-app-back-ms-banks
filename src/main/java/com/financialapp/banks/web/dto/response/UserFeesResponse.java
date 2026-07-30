package com.financialapp.banks.web.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserFeesResponse(
        List<AccountFeesEntry> accounts,
        List<CardFeesEntry> cards
) {
    public record AccountFeesEntry(
            String cbu,
            String accountType,
            String maintenanceFee,
            String transferFee,
            String currency,
            String ivaTreatment,
            String debitCreditTaxRate
    ) {}

    public record CardFeesEntry(
            String cardNumber,
            String annualFee,
            String internationalSurchargePct,
            String currency,
            String ivaTreatment
    ) {}
}
