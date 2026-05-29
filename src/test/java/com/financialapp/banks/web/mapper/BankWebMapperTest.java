package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.web.dto.response.AvailableBankResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BankWebMapperTest {

    private final BankWebMapper mapper = new BankWebMapper();

    @Test
    void mapsBankNameToAvailableBankResponse() {
        BankName sample = BankName.values()[0];
        AvailableBankResponse resp = mapper.toAvailableBank(sample);
        assertThat(resp.name()).isEqualTo(sample.name());
        assertThat(resp.displayName()).isEqualTo(sample.getDisplayName());
        assertThat(resp.logoUrl()).isEqualTo(sample.getLogoUrl());
    }

    @Test
    void mapsBankingCatalogToResponse() {
        var catalog = new com.financialapp.banks.domain.usecase.catalog.BankingCatalog(
                java.util.List.of("CHECKING"), java.util.List.of("CREDIT"),
                java.util.List.of("VISA"), java.util.List.of("REVOLVING"));
        var resp = mapper.toCatalogResponse(catalog);
        assertThat(resp.accountTypes()).containsExactly("CHECKING");
        assertThat(resp.cardTypes()).containsExactly("CREDIT");
        assertThat(resp.cardBrands()).containsExactly("VISA");
        assertThat(resp.cardBehaviors()).containsExactly("REVOLVING");
    }
}
