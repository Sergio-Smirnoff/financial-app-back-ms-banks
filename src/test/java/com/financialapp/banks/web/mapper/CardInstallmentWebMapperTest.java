package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CardInstallmentWebMapperTest {

    private final CardInstallmentWebMapper mapper = new CardInstallmentWebMapper();

    @Test
    void totalAmountAndAmountAreRenderedAsPlainStrings() {
        var installment = new CardInstallment(
                new CardInstallmentId(1L), "1234", "TV",
                Money.of(new BigDecimal("3000.00"), "ARS"),
                1, 3,
                Money.of(new BigDecimal("1000.00"), "ARS"),
                LocalDate.now(), false, null,
                LocalDateTime.now(), LocalDateTime.now());

        var resp = mapper.toResponse(installment);

        assertThat((Object) resp.totalAmount()).isInstanceOf(String.class);
        assertThat((Object) resp.totalAmount()).isEqualTo("3000.00");
        assertThat((Object) resp.amount()).isInstanceOf(String.class);
        assertThat((Object) resp.amount()).isEqualTo("1000.00");
        assertThat(resp.currency()).isEqualTo("ARS");
    }
}
