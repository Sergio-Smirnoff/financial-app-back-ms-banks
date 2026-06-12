package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccountWebMapperTest {

    private final AccountWebMapper mapper = new AccountWebMapper();

    @Test
    void balanceIsRenderedAsPlainDecimalString() {
        var account = new Account(AccountType.CHECKING, Cbu.from("0070001600000000123459"), "alias",
                Money.of(new BigDecimal("1234.50"), "ARS"),
                new UserId(1L), new BankNumber("007"), "Main", true,
                LocalDateTime.now(), LocalDateTime.now());
        var resp = mapper.toResponse(account);
        assertThat((Object) resp.balance()).isInstanceOf(String.class);
        assertThat((Object) resp.balance()).isEqualTo("1234.50");
        assertThat(resp.currency()).isEqualTo("ARS");
    }
}
