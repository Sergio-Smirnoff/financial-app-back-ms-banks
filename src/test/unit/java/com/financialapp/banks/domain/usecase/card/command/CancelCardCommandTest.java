package com.financialapp.banks.domain.usecase.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CancelCardCommandTest {

    @Test
    void exposesFieldsAndValueSemantics() {
        var a = new CancelCardCommand("4111111111111111", new UserId(1L));
        var b = new CancelCardCommand("4111111111111111", new UserId(1L));

        assertThat(a.cardNumber()).isEqualTo("4111111111111111");
        assertThat(a.userId()).isEqualTo(new UserId(1L));
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).contains("4111111111111111");
    }
}
