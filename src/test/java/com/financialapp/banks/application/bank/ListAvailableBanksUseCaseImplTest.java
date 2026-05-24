package com.financialapp.banks.application.bank;

import com.financialapp.banks.application.bank.impl.ListAvailableBanksUseCaseImpl;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.web.dto.response.AvailableBankResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListAvailableBanksUseCaseImplTest {

    private final ListAvailableBanksUseCaseImpl useCase = new ListAvailableBanksUseCaseImpl();

    @Test
    void execute_returnsAllEnumValues() {
        List<AvailableBankResponse> result = useCase.execute();

        assertThat(result).hasSize(BankName.values().length);
        assertThat(result).extracting(AvailableBankResponse::name)
                .containsExactlyInAnyOrder(
                        "GALICIA", "SANTANDER", "BBVA", "HIPOTECARIO", "MACRO",
                        "PATAGONIA", "NACION", "ICBC", "CITIBANK", "HSBC",
                        "SUPERVIELLE", "BANCO_COMAFI", "BANCO_DEL_CHUBUT");
    }

    @Test
    void execute_includesDisplayName() {
        List<AvailableBankResponse> result = useCase.execute();

        AvailableBankResponse galicia = result.stream()
                .filter(b -> b.name().equals("GALICIA"))
                .findFirst().orElseThrow();
        assertThat(galicia.displayName()).isEqualTo("Galicia");
    }
}
