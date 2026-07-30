package com.financialapp.banks.domain.usecase.fee.response;

import java.util.List;

public record UserFeesResult(
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
