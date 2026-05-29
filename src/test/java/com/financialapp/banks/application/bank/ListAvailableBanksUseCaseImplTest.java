package com.financialapp.banks.application.bank;

import com.financialapp.banks.application.bank.impl.ListAvailableBanksUseCaseImpl;
import com.financialapp.banks.domain.model.bank.BankName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListAvailableBanksUseCaseImplTest {

    private final ListAvailableBanksUseCaseImpl useCase = new ListAvailableBanksUseCaseImpl();

    @Test
    void execute_returnsAllEnumValues() {
        List<BankName> result = useCase.execute();

        assertThat(result).hasSize(BankName.values().length);
        assertThat(result).containsExactlyInAnyOrder(BankName.values());
    }
}
