package com.financialapp.banks.application.card;

import com.financialapp.banks.application.card.impl.UpdateCardUseCaseImpl;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.card.CardExpiredException;
import com.financialapp.banks.domain.exception.card.CardInvalidTypeException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.usecase.card.command.UpdateCardCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Branch coverage for UpdateCard: expired-date guard and the defensive unknown-type default. */
@ExtendWith(MockitoExtension.class)
class UpdateCardBranchesTest {

    @Mock CardRepository cardRepository;
    UpdateCardUseCaseImpl useCase;

    private static final UserId USER = new UserId(1L);
    private static final String PAN = "4111111111111111";

    @BeforeEach
    void setUp() {
        useCase = new UpdateCardUseCaseImpl(cardRepository);
    }

    private CardDetails details() {
        return new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
    }

    @Test
    void rejectsPastExpiryDate() {
        CreditCard card = new CreditCard(CardNumber.from(PAN), USER, new BankNumber("007"), details(),
                LocalDateTime.now(), LocalDateTime.now());
        when(cardRepository.findByCardNumberAndUserId(PAN, USER)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> useCase.execute(new UpdateCardCommand(PAN, USER, YearMonth.now().minusMonths(1), null, null)))
                .isInstanceOf(CardExpiredException.class);
    }

    @Test
    void rejectsUnknownCardSubtype() {
        // A test-only Card subtype drives the defensive default branch of the switch.
        when(cardRepository.findByCardNumberAndUserId(PAN, USER)).thenReturn(Optional.of(new FakeCard()));

        assertThatThrownBy(() -> useCase.execute(new UpdateCardCommand(PAN, USER, null, 15, 5)))
                .isInstanceOf(CardInvalidTypeException.class);
    }

    /** Minimal non-Credit/Debit Card to exercise the unreachable default branch. */
    private static final class FakeCard extends Card {
        FakeCard() {
            super(CardNumber.from(PAN), USER, new BankNumber("007"),
                    new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                            YearMonth.now().plusYears(2), new CardBilling(20, 10)),
                    LocalDateTime.now(), LocalDateTime.now());
        }
    }
}
