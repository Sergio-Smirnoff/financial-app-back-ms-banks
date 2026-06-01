package com.financialapp.banks.web.dto.response;

public record AvailableBankResponse(
        String bankNumber,
        String name,
        String logoUrl
) {}
