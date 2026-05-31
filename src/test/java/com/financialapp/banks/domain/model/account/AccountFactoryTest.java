package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class AccountFactoryTest {

    private static final Money BALANCE =
            new Money(new BigDecimal("100.00"), Currency.getInstance("USD"));
    private static final UserId USER_ID = new UserId(1L);
    private static final LocalDateTime NOW = LocalDateTime.now();

    private Account create(AccountType type) {
        return Account.create(type, Cbu.from("0070001600000000123459"), "alias", BALANCE,
                USER_ID, new BankNumber("007"), "My Account", true, NOW, NOW);
    }

    @Test
    void create_checkingReturnsCheckingAccount() {
        Account account = create(AccountType.CHECKING);

        assertThat(account).isInstanceOf(CheckingAccount.class);
        assertThat(account.cbu().value()).isEqualTo("0070001600000000123459");
        assertThat(account.balance().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void create_savingsReturnsSavingsAccount() {
        Account account = create(AccountType.SAVINGS);

        assertThat(account).isInstanceOf(SavingsAccount.class);
        assertThat(account.cbu().value()).isEqualTo("0070001600000000123459");
        assertThat(account.balance().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void create_investmentReturnsInvestmentAccount() {
        Account account = create(AccountType.INVESTMENT);

        assertThat(account).isInstanceOf(InvestmentAccount.class);
        assertThat(account.cbu().value()).isEqualTo("0070001600000000123459");
        assertThat(account.balance().amount()).isEqualByComparingTo("100.00");
    }
}
