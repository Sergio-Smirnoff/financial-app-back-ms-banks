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
}
