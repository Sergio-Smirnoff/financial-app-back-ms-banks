package com.financialapp.banks.domain.usecase.loan.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.loan.LoanId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CancelLoanCommandTest {

    @Test
    void exposesFieldsAndValueSemantics() {
        var a = new CancelLoanCommand(new LoanId(5L), new UserId(1L));
        var b = new CancelLoanCommand(new LoanId(5L), new UserId(1L));

        assertThat(a.id()).isEqualTo(new LoanId(5L));
        assertThat(a.userId()).isEqualTo(new UserId(1L));
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).contains("5");
    }
}
