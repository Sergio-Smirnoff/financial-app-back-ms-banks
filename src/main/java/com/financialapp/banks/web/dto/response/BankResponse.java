package com.financialapp.banks.web.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record BankResponse(
        String name,
        String logoUrl,
        List<AccountResponse> accounts,
        Map<String, String> totalBalances,
        int accountsCount
) {}
