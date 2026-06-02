package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.usecase.catalog.BankingCatalog;
import com.financialapp.banks.web.dto.response.AccountResponse;
import com.financialapp.banks.web.dto.response.AvailableBankResponse;
import com.financialapp.banks.web.dto.response.BankResponse;
import com.financialapp.banks.web.dto.response.BankingCatalogResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BankWebMapper {

    public BankResponse toResponse(Bank bank, List<AccountResponse> accounts) {
        if (bank == null) return null;
        Map<String, String> totalBalances = accounts.stream()
                .map(account -> Money.of(account.balance(), account.currency()))
                .collect(Collectors.groupingBy(
                        money -> money.currency().getCurrencyCode(),
                        Collectors.collectingAndThen(
                                Collectors.reducing(Money::add),
                                total -> total.map(money -> money.amount().toPlainString()).orElse("0"))));
        return BankResponse.builder()
                .bankNumber(bank.bankNumber().value())
                .name(bank.name())
                .logoUrl(bank.logo() != null ? bank.logo().url() : null)
                .accounts(accounts)
                .totalBalances(totalBalances)
                .accountsCount(accounts.size())
                .build();
    }

    public AvailableBankResponse toAvailableBank(Bank bank) {
        return new AvailableBankResponse(
                bank.bankNumber().value(),
                bank.name(),
                bank.logo() != null ? bank.logo().url() : null);
    }

    public BankingCatalogResponse toCatalogResponse(BankingCatalog catalog) {
        return new BankingCatalogResponse(
                catalog.accountTypes(), catalog.cardTypes(), catalog.cardBrands(), catalog.cardBehaviors());
    }
}
