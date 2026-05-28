package com.financialapp.banks.domain.usecase.catalog;

import com.financialapp.banks.web.dto.response.BankingCatalogResponse;

public interface GetBankingCatalogUseCase {
    BankingCatalogResponse execute();
}
