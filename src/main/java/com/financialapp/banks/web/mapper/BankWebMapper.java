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
        Map<String, BigDecimal> summed = accounts.stream()
                .collect(Collectors.groupingBy(
                        AccountResponse::currency,
                        Collectors.reducing(BigDecimal.ZERO,
                                account -> new BigDecimal(account.balance()), BigDecimal::add)));
        Map<String, String> totalBalances = summed.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, currencyTotal -> currencyTotal.getValue().toPlainString()));
        return BankResponse.builder()
                .name(bank.name().name())
                .logoUrl(bank.logo() != null ? bank.logo().url() : null)
                .accounts(accounts)
                .totalBalances(totalBalances)
                .accountsCount(accounts.size())
                .build();
    }

    public AvailableBankResponse toAvailableBank(BankName bankName) {
        return new AvailableBankResponse(bankName.name(), bankName.getDisplayName(), bankName.getLogoUrl());
    }

    public BankingCatalogResponse toCatalogResponse(BankingCatalog catalog) {
        return new BankingCatalogResponse(
                catalog.accountTypes(), catalog.cardTypes(), catalog.cardBrands(), catalog.cardBehaviors());
    }
}
