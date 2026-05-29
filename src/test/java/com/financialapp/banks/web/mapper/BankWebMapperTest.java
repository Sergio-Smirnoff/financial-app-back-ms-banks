package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.web.dto.response.AccountResponse;
import com.financialapp.banks.web.dto.response.AvailableBankResponse;
import com.financialapp.banks.web.dto.response.BankResponse;
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

    @Test
    void totalBalancesSumPerCurrencyAsStrings() {
        var a1 = AccountResponse.builder().currency("ARS").balance("100.00").build();
        var a2 = AccountResponse.builder().currency("ARS").balance("50.50").build();
        var a3 = AccountResponse.builder().currency("USD").balance("10.00").build();
        var bank = new Bank(BankName.values()[0], null);
        BankResponse resp = mapper.toResponse(bank, java.util.List.of(a1, a2, a3));
        assertThat(resp.totalBalances()).containsEntry("ARS", "150.50").containsEntry("USD", "10.00");
        assertThat((Object) resp.totalBalances().get("ARS")).isInstanceOf(String.class);
    }
}
