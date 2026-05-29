package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.usecase.catalog.BankingCatalog;
import com.financialapp.banks.web.dto.response.AccountResponse;
import com.financialapp.banks.web.dto.response.AvailableBankResponse;
import com.financialapp.banks.web.dto.response.BankResponse;
import com.financialapp.banks.web.dto.response.BankingCatalogResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BankWebMapper {

    public BankResponse toResponse(Bank bank, List<AccountResponse> accounts) {
        if (bank == null) return null;
        Map<String, BigDecimal> totalBalances = accounts.stream()
                .collect(Collectors.groupingBy(
                        AccountResponse::currency,
                        Collectors.reducing(BigDecimal.ZERO, AccountResponse::balance, BigDecimal::add)
                ));
        return BankResponse.builder()
                .name(bank.name().name())
                .logoUrl(bank.logo() != null ? bank.logo().url() : null)
                .accounts(accounts)
                .totalBalances(totalBalances)
                .accountsCount(accounts.size())
                .build();
    }

    public AvailableBankResponse toAvailableBank(BankName bank) {
        return new AvailableBankResponse(bank.name(), bank.getDisplayName(), bank.getLogoUrl());
    }

    public BankingCatalogResponse toCatalogResponse(BankingCatalog c) {
        return new BankingCatalogResponse(
                c.accountTypes(), c.cardTypes(), c.cardBrands(), c.cardBehaviors());
    }
}
