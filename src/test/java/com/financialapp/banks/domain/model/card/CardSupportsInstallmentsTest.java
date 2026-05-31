package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.card.CardInstallmentNotSupportedException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardSupportsInstallmentsTest {

    private Card card(CardBehavior behavior) {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM,
                behavior, YearMonth.now().plusYears(2), new CardBilling(20, 10));
        CardNumber number = CardNumber.from("4111111111111111");
        return behavior == CardBehavior.INSTANT_PAYMENT
                ? new DebitCard(number, new UserId(1L), new BankNumber("007"), details, LocalDateTime.now(), LocalDateTime.now())
                : new CreditCard(number, new UserId(1L), new BankNumber("007"), details, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void ensureSupportsInstallments_noOpForCreditBehavior() {
        card(CardBehavior.CREDIT).ensureSupportsInstallments();
    }

    @Test
    void ensureSupportsInstallments_throwsForInstantPayment() {
        assertThatThrownBy(() -> card(CardBehavior.INSTANT_PAYMENT).ensureSupportsInstallments())
                .isInstanceOf(CardInstallmentNotSupportedException.class);
    }
}
