package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.card.CardInstallmentAlreadyPaidException;
import com.financialapp.banks.domain.exception.card.CardInstallmentMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardInstallmentTest {

    private static final Currency USD = Currency.getInstance("USD");

    private CardInstallment unpaid() {
        return new CardInstallment(
                new CardInstallmentId(7L),
                "1234567890123456",
                "New Mac",
                new Money(new BigDecimal("3000"), USD),
                1,
                3,
                new Money(new BigDecimal("1000.00"), USD),
                LocalDate.of(2026, 5, 1),
                false,
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
    }

    @Test
    void pay_returnsPaidCopy() {
        CardInstallment installment = unpaid();
        LocalDate paidDate = LocalDate.of(2026, 5, 10);

        CardInstallment paid = installment.pay(paidDate);

        assertThat(paid.paid()).isTrue();
        assertThat(paid.paidDate()).isEqualTo(paidDate);
        assertThat(paid.id()).isEqualTo(installment.id());
        assertThat(paid.cardNumber()).isEqualTo(installment.cardNumber());
        assertThat(paid.description()).isEqualTo(installment.description());
        assertThat(paid.totalAmount()).isEqualTo(installment.totalAmount());
        assertThat(paid.installmentNumber()).isEqualTo(installment.installmentNumber());
        assertThat(paid.totalInstallments()).isEqualTo(installment.totalInstallments());
        assertThat(paid.amount()).isEqualTo(installment.amount());
        assertThat(paid.dueDate()).isEqualTo(installment.dueDate());
        assertThat(paid.createdAt()).isEqualTo(installment.createdAt());
        assertThat(paid.updatedAt()).isAfterOrEqualTo(installment.updatedAt());
    }

    @Test
    void pay_throwsWhenAlreadyPaid() {
        CardInstallment paid = unpaid().pay(LocalDate.of(2026, 5, 10));

        assertThatThrownBy(() -> paid.pay(LocalDate.of(2026, 5, 11)))
                .isInstanceOf(CardInstallmentAlreadyPaidException.class);
    }

    @Test
    void ensureBelongsTo_noOpWhenMatches() {
        unpaid().ensureBelongsTo("1234567890123456");
    }

    @Test
    void ensureBelongsTo_throwsWhenMismatch() {
        assertThatThrownBy(() -> unpaid().ensureBelongsTo("9999999999999999"))
                .isInstanceOf(CardInstallmentMismatchException.class);
    }

    @Test
    void schedule_splitsEvenly() {
        Money total = new Money(new BigDecimal("3000"), USD);

        List<CardInstallment> installments = CardInstallment.schedule(
                "1234", "New Mac", total, 3, LocalDate.of(2026, 5, 1));

        assertThat(installments).hasSize(3);
        assertThat(installments.get(0).amount().amount()).isEqualByComparingTo("1000.00");
        assertThat(installments.get(1).amount().amount()).isEqualByComparingTo("1000.00");
        assertThat(installments.get(2).amount().amount()).isEqualByComparingTo("1000.00");
        assertThat(installments.get(0).dueDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(installments.get(1).dueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(installments.get(2).dueDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        installments.forEach(i -> {
            assertThat(i.paid()).isFalse();
            assertThat(i.paidDate()).isNull();
            assertThat(i.id()).isEqualTo(new CardInstallmentId(null));
            assertThat(i.cardNumber()).isEqualTo("1234");
            assertThat(i.description()).isEqualTo("New Mac");
            assertThat(i.totalAmount()).isEqualTo(total);
            assertThat(i.totalInstallments()).isEqualTo(3);
        });
        assertThat(installments.get(0).installmentNumber()).isEqualTo(1);
        assertThat(installments.get(2).installmentNumber()).isEqualTo(3);
    }

    @Test
    void schedule_putsRoundingRemainderOnLastInstallment() {
        // 100 / 3 = 33.33 each -> last = 100 - 33.33*2 = 33.34
        Money total = new Money(new BigDecimal("100"), USD);

        List<CardInstallment> installments = CardInstallment.schedule(
                "1234", "Stuff", total, 3, LocalDate.of(2026, 5, 1));

        assertThat(installments.get(0).amount().amount()).isEqualByComparingTo("33.33");
        assertThat(installments.get(1).amount().amount()).isEqualByComparingTo("33.33");
        assertThat(installments.get(2).amount().amount()).isEqualByComparingTo("33.34");
    }
}
