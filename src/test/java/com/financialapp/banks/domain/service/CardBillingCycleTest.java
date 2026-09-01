package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.exception.card.InvalidCardBillingException;
import com.financialapp.banks.domain.model.card.BillingPeriod;
import com.financialapp.banks.domain.model.card.CardBilling;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardBillingCycleTest {

    private CardBillingCycle billingCycle;

    @BeforeEach
    void setUp() {
        billingCycle = new CardBillingCycle();
    }

    @Test
    void currentPeriod_close5Due15_sameMonth() {
        CardBilling billing = new CardBilling(5, 15);
        LocalDate today = LocalDate.of(2026, 7, 3);

        BillingPeriod period = billingCycle.currentPeriod(billing, today);

        assertThat(period.closingDate()).isEqualTo(LocalDate.of(2026, 7, 5));
        assertThat(period.dueDate()).isEqualTo(LocalDate.of(2026, 7, 15));
        assertThat(period.statementOpen()).isTrue();
    }

    @Test
    void currentPeriod_close28Due5_dueNextMonth() {
        CardBilling billing = new CardBilling(28, 5);
        LocalDate today = LocalDate.of(2026, 7, 20);

        BillingPeriod period = billingCycle.currentPeriod(billing, today);

        assertThat(period.closingDate()).isEqualTo(LocalDate.of(2026, 7, 28));
        assertThat(period.dueDate()).isEqualTo(LocalDate.of(2026, 8, 5));
        assertThat(period.statementOpen()).isTrue();
    }

    @Test
    void currentPeriod_closingDay31InApril_clampsTo30th() {
        CardBilling billing = new CardBilling(31, 15);
        LocalDate today = LocalDate.of(2026, 4, 10);

        BillingPeriod period = billingCycle.currentPeriod(billing, today);

        assertThat(period.closingDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(period.dueDate()).isEqualTo(LocalDate.of(2026, 5, 15));
        assertThat(period.statementOpen()).isTrue();
    }

    @Test
    void currentPeriod_closingDay30InFebruary2026_clampsToFeb28() {
        CardBilling billing = new CardBilling(30, 10);
        LocalDate today = LocalDate.of(2026, 2, 1);

        BillingPeriod period = billingCycle.currentPeriod(billing, today);

        assertThat(period.closingDate()).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(period.dueDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(period.statementOpen()).isTrue();
    }

    @Test
    void currentPeriod_todayBeforeOnAfterClosing_statementOpenTrueTrueFalse() {
        CardBilling billing = new CardBilling(15, 25);

        LocalDate before = LocalDate.of(2026, 7, 10);
        BillingPeriod periodBefore = billingCycle.currentPeriod(billing, before);
        assertThat(periodBefore.statementOpen()).isTrue();

        LocalDate onClosing = LocalDate.of(2026, 7, 15);
        BillingPeriod periodOn = billingCycle.currentPeriod(billing, onClosing);
        assertThat(periodOn.statementOpen()).isTrue();

        LocalDate after = LocalDate.of(2026, 7, 16);
        BillingPeriod periodAfter = billingCycle.currentPeriod(billing, after);
        assertThat(periodAfter.statementOpen()).isFalse();
    }

    @Test
    void cardBilling_invalidClosingOrDueDay_throwsInvalidCardBillingException() {
        assertThatThrownBy(() -> new CardBilling(0, 15))
                .isInstanceOf(InvalidCardBillingException.class);

        assertThatThrownBy(() -> new CardBilling(15, 32))
                .isInstanceOf(InvalidCardBillingException.class);
    }
}
