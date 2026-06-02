package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountWebMapperTest {

    private final AccountWebMapper mapper = new AccountWebMapper();

    @Test
    void balanceIsRenderedAsPlainDecimalString() {
        var account = new CheckingAccount(Cbu.from("0070001600000000123459"), "alias",
                Money.of(new BigDecimal("1234.50"), "ARS"),
                new UserId(1L), new BankNumber("007"), "Main", true,
                LocalDateTime.now(), LocalDateTime.now());
        var resp = mapper.toResponse(account);
        assertThat((Object) resp.balance()).isInstanceOf(String.class);
        assertThat((Object) resp.balance()).isEqualTo("1234.50");
        assertThat(resp.currency()).isEqualTo("ARS");
    }

    private Account account(String type) {
        var cbu = Cbu.from("0070001600000000123459");
        var balance = Money.of(new BigDecimal("100.00"), "ARS");
        var user = new UserId(1L);
        var bank = new BankNumber("007");
        var now = LocalDateTime.now();
        return switch (type) {
            case "CHECKING" -> new CheckingAccount(cbu, "a", balance, user, bank, "Main", true, now, now);
            case "SAVINGS" -> new SavingsAccount(cbu, "a", balance, user, bank, "Main", true, now, now);
            default -> new InvestmentAccount(cbu, "a", balance, user, bank, "Main", true, now, now);
        };
    }

    @Test
    void toResponse_returnsNull_whenAccountNull() {
        // Given a null account / When mapped / Then null (the null guard)
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void type_resolvesForEachSubtype() {
        // Given each concrete account subtype / When mapped / Then the discriminator matches
        assertThat(mapper.toResponse(account("CHECKING")).type()).isEqualTo("CHECKING");
        assertThat(mapper.toResponse(account("SAVINGS")).type()).isEqualTo("SAVINGS");
        assertThat(mapper.toResponse(account("INVESTMENT")).type()).isEqualTo("INVESTMENT");
    }

    @Test
    void type_throwsForUnknownSubtype() {
        // Given a test-only Account subtype / When mapped / Then resolveType rejects it
        Account fake = new Account(Cbu.from("0070001600000000123459"), "a",
                Money.of(new BigDecimal("1.00"), "ARS"), new UserId(1L), new BankNumber("007"), "Fake", true,
                LocalDateTime.now(), LocalDateTime.now()) {
            @Override
            public Account withBalance(Money newBalance, LocalDateTime updatedAt) {
                return this;
            }
        };
        assertThatThrownBy(() -> mapper.toResponse(fake)).isInstanceOf(IllegalStateException.class);
    }
}
