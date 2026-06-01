package com.financialapp.banks.application.catalog.impl;

import com.financialapp.banks.domain.usecase.catalog.BankingCatalog;
import com.financialapp.banks.domain.usecase.catalog.GetBankingCatalogUseCase;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GetBankingCatalogUseCaseImpl implements GetBankingCatalogUseCase {

    @Override
    public BankingCatalog execute() {
        return new BankingCatalog(
                names(AccountType.values()),
                names(CardType.values()),
                names(CardBrand.values()),
                names(CardBehavior.values()));
    }

    private List<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
