package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.account.AccountCurrencyMismatchException;
import com.financialapp.banks.domain.exception.account.AccountInsufficientFundsException;
import com.financialapp.banks.domain.exception.account.AccountInvestmentRestrictionException;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.bank.BankName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountBehaviorTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Currency USD = Currency.getInstance("USD");
    private static final LocalDateTime NOW = LocalDateTime.now();

    private CheckingAccount checking(BigDecimal balance) {
        return new CheckingAccount("1234567890123456789012", "alias",
                new Money(balance, ARS),
                new UserId(1L), BankName.GALICIA, "My acc", true,
                NOW, NOW);
    }

    private InvestmentAccount investment(BigDecimal balance) {
        return new InvestmentAccount("1234567890123456789012", "alias",
                new Money(balance, ARS),
                new UserId(1L), BankName.GALICIA, "Inv", true,
                NOW, NOW);
    }

    private Money ars(String amount) {
        return new Money(new BigDecimal(amount), ARS);
    }

    @Test
    void debit_reducesBalanceByAmount() {
        CheckingAccount acc = checking(new BigDecimal("100.00"));

        Account result = acc.debit(ars("30.00"), NOW);

        assertThat(result.balance().amount()).isEqualByComparingTo("70.00");
    }

    @Test
    void debit_returnsNewInstanceLeavingOriginalUnchanged() {
        CheckingAccount acc = checking(new BigDecimal("100.00"));

        Account result = acc.debit(ars("30.00"), NOW);

        assertThat(result).isNotSameAs(acc);
        assertThat(acc.balance().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void debit_moreThanBalance_throwsInsufficientFunds() {
        CheckingAccount acc = checking(new BigDecimal("10.00"));

        assertThatThrownBy(() -> acc.debit(ars("50.00"), NOW))
                .isInstanceOf(AccountInsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void debit_differentCurrency_throwsCurrencyMismatch() {
        CheckingAccount acc = checking(new BigDecimal("100.00"));

        assertThatThrownBy(() -> acc.debit(new Money(new BigDecimal("10.00"), USD), NOW))
                .isInstanceOf(AccountCurrencyMismatchException.class);
    }

    @Test
    void credit_increasesBalanceByAmount() {
        CheckingAccount acc = checking(new BigDecimal("100.00"));

        Account result = acc.credit(ars("50.00"), NOW);

        assertThat(result.balance().amount()).isEqualByComparingTo("150.00");
    }

    @Test
    void credit_differentCurrency_throwsCurrencyMismatch() {
        CheckingAccount acc = checking(new BigDecimal("100.00"));

        assertThatThrownBy(() -> acc.credit(new Money(new BigDecimal("10.00"), USD), NOW))
                .isInstanceOf(AccountCurrencyMismatchException.class);
    }

    @Test
    void investmentAccount_debit_throwsRestriction() {
        InvestmentAccount acc = investment(new BigDecimal("100.00"));

        assertThatThrownBy(() -> acc.debit(ars("10.00"), NOW))
                .isInstanceOf(AccountInvestmentRestrictionException.class)
                .hasMessageContaining("investment account");
    }

    @Test
    void investmentAccount_credit_throwsRestriction() {
        InvestmentAccount acc = investment(new BigDecimal("100.00"));

        assertThatThrownBy(() -> acc.credit(ars("10.00"), NOW))
                .isInstanceOf(AccountInvestmentRestrictionException.class)
                .hasMessageContaining("investment account");
    }

    @Test
    void isLowBalance_belowThreshold_returnsTrue() {
        CheckingAccount acc = checking(new BigDecimal("100.00"));

        assertThat(acc.isLowBalance(ars("500.00"))).isTrue();
    }

    @Test
    void isLowBalance_atOrAboveThreshold_returnsFalse() {
        CheckingAccount acc = checking(new BigDecimal("500.00"));

        assertThat(acc.isLowBalance(ars("500.00"))).isFalse();
        assertThat(checking(new BigDecimal("800.00")).isLowBalance(ars("500.00"))).isFalse();
    }
}
