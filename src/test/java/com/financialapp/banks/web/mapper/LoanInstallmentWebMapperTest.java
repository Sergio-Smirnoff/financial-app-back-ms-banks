package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoanInstallmentWebMapperTest {

    private final LoanInstallmentWebMapper mapper = new LoanInstallmentWebMapper();

    @Test
    void amountIsRenderedAsPlainDecimalString() {
        var installment = new LoanInstallment(
                new LoanInstallmentId(1L), new LoanId(1L), 1,
                Money.of(new BigDecimal("1250.75"), "ARS"),
                LocalDate.now(), false, null,
                LocalDateTime.now(), LocalDateTime.now());

        var resp = mapper.toResponse(installment);

        assertThat((Object) resp.amount()).isInstanceOf(String.class);
        assertThat((Object) resp.amount()).isEqualTo("1250.75");
    }
}
