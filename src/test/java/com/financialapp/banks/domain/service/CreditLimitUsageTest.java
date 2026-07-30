package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreditLimitUsageTest {

    private final CreditLimitUsage creditLimitUsage = new CreditLimitUsage();
    private final Currency ARS = Currency.getInstance("ARS");

    @Test
    void usedAmount_sumsOnlyUnpaidInstallments() {
        CardInstallment unpaid1 = createInstallment(1, new BigDecimal("200000.00"), false);
        CardInstallment unpaid2 = createInstallment(2, new BigDecimal("210000.00"), false);
        CardInstallment paid = createInstallment(3, new BigDecimal("100000.00"), true);

        Money used = creditLimitUsage.usedAmount(List.of(unpaid1, unpaid2, paid));

        assertThat(used).isNotNull();
        assertThat(used.amount()).isEqualByComparingTo("410000.00");
        assertThat(used.currency()).isEqualTo(ARS);
    }

    @Test
    void usedAmount_returnsNullWhenEmptyOrNull() {
        assertThat(creditLimitUsage.usedAmount(null)).isNull();
        assertThat(creditLimitUsage.usedAmount(List.of())).isNull();
    }

    @Test
    void usedPercent_returnsZeroWhenCreditLimitIsNull() {
        Money usedAmount = new Money(new BigDecimal("410000.00"), ARS);
        BigDecimal percent = creditLimitUsage.usedPercent(null, usedAmount);
        assertThat(percent).isEqualByComparingTo("0.00");
    }

    @Test
    void usedPercent_returnsZeroWhenUsedAmountIsNull() {
        Money creditLimit = new Money(new BigDecimal("1000000.00"), ARS);
        BigDecimal percent = creditLimitUsage.usedPercent(creditLimit, null);
        assertThat(percent).isEqualByComparingTo("0.00");
    }

    @Test
    void usedPercent_calculatesPercentageCorrectly() {
        Money creditLimit = new Money(new BigDecimal("1000000.00"), ARS);
        Money usedAmount = new Money(new BigDecimal("410000.00"), ARS);

        BigDecimal percent = creditLimitUsage.usedPercent(creditLimit, usedAmount);
        assertThat(percent).isEqualByComparingTo("41.00");
    }

    private CardInstallment createInstallment(int number, BigDecimal amount, boolean paid) {
        LocalDateTime now = LocalDateTime.now();
        return new CardInstallment(
                new CardInstallmentId((long) number),
                "1234567890123456",
                "Purchase " + number,
                new Money(amount, ARS),
                number,
                3,
                new Money(amount, ARS),
                LocalDate.now(),
                paid,
                paid ? LocalDate.now() : null,
                now,
                now
        );
    }
}
