package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class CardFactoryTest {

    private static final String NUMBER = "4111111111111111";
    private static final UserId USER_ID = new UserId(1L);
    private static final LocalDateTime NOW = LocalDateTime.now();

    private CardDetails details(CardBehavior behavior) {
        return new CardDetails(CardBrand.VISA, CardType.PLATINUM, behavior,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
    }

    private Card create(CardBehavior behavior) {
        return Card.create(NUMBER, USER_ID, new BankNumber("007"), details(behavior), NOW, NOW);
    }

    @Test
    void create_instantPaymentReturnsDebitCard() {
        Card card = create(CardBehavior.INSTANT_PAYMENT);

        assertThat(card).isInstanceOf(DebitCard.class);
        assertThat(card.cardNumber().value()).isEqualTo(NUMBER);
    }

    @Test
    void create_creditReturnsCreditCard() {
        Card card = create(CardBehavior.CREDIT);

        assertThat(card).isInstanceOf(CreditCard.class);
        assertThat(card.cardNumber().value()).isEqualTo(NUMBER);
    }
}
