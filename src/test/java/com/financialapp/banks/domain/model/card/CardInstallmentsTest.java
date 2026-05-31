package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.card.CardInstallmentNotSupportedException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardInstallmentsTest {

    private static final Currency ARS = Currency.getInstance("ARS");

    private Card creditCard() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.CREDIT,
                YearMonth.of(2030, 1), new CardBilling(20, 10));
        return Card.create("1234567890123456", new UserId(1L), new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void registerExpense_appends_a_schedule() {
        Card card = creditCard();
        card.registerExpense("TV", new Money(new BigDecimal("300.00"), ARS), 3, LocalDate.of(2026, 7, 1));
        assertThat(card.installments()).hasSize(3);
        assertThat(card.installments().get(0).description()).isEqualTo("TV");
    }

    @Test
    void debit_card_rejects_installments() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.INSTANT_PAYMENT,
                YearMonth.of(2030, 1), new CardBilling(20, 10));
        Card debit = Card.create("1234567890123456", new UserId(1L), new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now());
        assertThatThrownBy(() -> debit.registerExpense("X", new Money(new BigDecimal("10.00"), ARS), 1, LocalDate.now()))
                .isInstanceOf(CardInstallmentNotSupportedException.class);
    }

    @Test
    void hasInstallmentMatching_detects_duplicate() {
        Card card = creditCard();
        card.registerExpense("TV", new Money(new BigDecimal("300.00"), ARS), 1, LocalDate.of(2026, 7, 1));
        boolean dup = card.hasInstallmentMatching("TV", new Money(new BigDecimal("300.00"), ARS), LocalDate.of(2026, 7, 1));
        assertThat(dup).isTrue();
    }

    @Test
    void payInstallment_marks_paid() {
        Card card = creditCard();
        card.registerExpense("TV", new Money(new BigDecimal("300.00"), ARS), 1, LocalDate.of(2026, 7, 1));
        CardInstallmentId id = card.installments().get(0).id();

        CardInstallment paid = card.payInstallment(id, LocalDate.of(2026, 7, 5), "0001").installment();

        assertThat(paid.paid()).isTrue();
        assertThat(card.installments().get(0).paid()).isTrue();
    }
}
