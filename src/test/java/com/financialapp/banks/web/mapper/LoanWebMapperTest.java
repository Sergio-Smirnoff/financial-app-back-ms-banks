package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoanWebMapperTest {

    private final LoanWebMapper mapper = new LoanWebMapper();

    @Test
    void principalAndInterestRateAreRenderedAsPlainStrings() {
        var loan = new Loan(
                new LoanId(1L), new UserId(1L), BankName.values()[0], "Car loan",
                Money.of(new BigDecimal("150000.00"), "ARS"),
                new BigDecimal("0.4500"),
                12, 12, AmortizationType.FRENCH,
                LocalDate.now(), true, LocalDateTime.now(), LocalDateTime.now());

        var resp = mapper.toResponse(loan);

        assertThat((Object) resp.principal()).isInstanceOf(String.class);
        assertThat((Object) resp.principal()).isEqualTo("150000.00");
        assertThat((Object) resp.interestRate()).isInstanceOf(String.class);
        assertThat((Object) resp.interestRate()).isEqualTo("0.4500");
        assertThat(resp.currency()).isEqualTo("ARS");
    }
}
