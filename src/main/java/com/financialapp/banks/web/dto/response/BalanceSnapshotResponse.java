package com.financialapp.banks.web.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record BalanceSnapshotResponse(
        LocalDate snapshotDate,
        Map<String, String> cashByCurrency,
        Map<String, String> cardDebtByCurrency,
        Map<String, String> loanDebtByCurrency
) {}
