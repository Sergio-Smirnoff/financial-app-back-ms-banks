package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.BankName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccountWebMapperTest {

    private final AccountWebMapper mapper = new AccountWebMapper();

    @Test
    void balanceIsRenderedAsPlainDecimalString() {
        var account = new CheckingAccount("0001", "alias",
                Money.of(new BigDecimal("1234.50"), "ARS"),
                new UserId(1L), BankName.values()[0], "Main", true,
                LocalDateTime.now(), LocalDateTime.now());
        var resp = mapper.toResponse(account);
        assertThat((Object) resp.balance()).isInstanceOf(String.class);
        assertThat((Object) resp.balance()).isEqualTo("1234.50");
        assertThat(resp.currency()).isEqualTo("ARS");
    }
}
