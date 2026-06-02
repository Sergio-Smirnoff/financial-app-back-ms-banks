package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.bank.Logo;
import com.financialapp.banks.web.dto.response.BankResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Null-guard and logo branches for the web mappers not covered by their happy-path tests. */
class WebMapperBranchesTest {

    @Test
    void loanWebMapper_returnsNull_whenLoanNull() {
        assertThat(new LoanWebMapper().toResponse(null)).isNull();
    }

    @Test
    void cardInstallmentWebMapper_returnsNull_whenInstallmentNull() {
        assertThat(new CardInstallmentWebMapper().toResponse(null)).isNull();
    }

    @Test
    void loanInstallmentWebMapper_returnsNull_whenInstallmentNull() {
        assertThat(new LoanInstallmentWebMapper().toResponse(null)).isNull();
    }

    @Test
    void bankWebMapper_returnsNull_whenBankNull() {
        assertThat(new BankWebMapper().toResponse(null, List.of())).isNull();
    }

    @Test
    void bankWebMapper_setsLogoUrl_whenLogoPresent() {
        // Given a bank with a logo (the logo != null branch)
        Bank bank = new Bank(new BankNumber("007"), "GALICIA", new Logo("http://logo"));
        BankResponse response = new BankWebMapper().toResponse(bank, List.of());
        assertThat(response.logoUrl()).isEqualTo("http://logo");
    }

    @Test
    void bankWebMapper_nullLogoUrl_whenLogoNull() {
        // Given a bank with no logo (the logo == null branch)
        Bank bank = new Bank(new BankNumber("007"), "GALICIA", null);
        BankResponse response = new BankWebMapper().toResponse(bank, List.of());
        assertThat(response.logoUrl()).isNull();
    }

    @Test
    void bankWebMapper_toAvailableBank_nullLogoUrl_whenLogoNull() {
        // Given a bank with no logo / When mapped to the available-bank view
        var response = new BankWebMapper().toAvailableBank(new Bank(new BankNumber("007"), "GALICIA", null));
        assertThat(response.logoUrl()).isNull();
    }
}
