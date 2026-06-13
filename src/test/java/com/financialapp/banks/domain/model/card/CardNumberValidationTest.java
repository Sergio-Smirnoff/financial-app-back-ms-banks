package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.card.InvalidCardCheckDigitException;
import com.financialapp.banks.domain.exception.card.InvalidCardNumberException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardNumberValidationTest {

    @Test
    void from_with16DigitsBadCheckDigit_throwsCheckDigitErrorNotLengthError() {
        assertThatThrownBy(() -> CardNumber.from("0140000000000000"))
                .isInstanceOf(InvalidCardCheckDigitException.class)
                .extracting("error")
                .isEqualTo(DomainError.INVALID_CARD_CHECK_DIGIT);
    }

    @Test
    void from_with15Digits_autoCompletesCheckDigitToValidCard() {
        CardNumber number = CardNumber.from("014000000000000");

        assertThat(number.value()).hasSize(16);
        assertThat(number.value()).startsWith("014000000000000");
        assertThat(number.value()).isEqualTo("0140000000000001");
    }

    @Test
    void from_with15Digits_isStableWhenReparsedAsFull16() {
        String completed = CardNumber.from("014000000000000").value();

        assertThat(CardNumber.from(completed).value()).isEqualTo(completed);
    }

    @Test
    void from_withWrongLength_throwsInvalidCardNumber() {
        assertThatThrownBy(() -> CardNumber.from("123"))
                .isInstanceOf(InvalidCardNumberException.class)
                .extracting("error")
                .isEqualTo(DomainError.INVALID_CARD_NUMBER);
    }

    @Test
    void from_withNull_throwsInvalidCardNumber() {
        assertThatThrownBy(() -> CardNumber.from(null))
                .isInstanceOf(InvalidCardNumberException.class);
    }

    @Test
    void from_withValidFull16_stillParses() {
        assertThat(CardNumber.from("4111111111111111").value()).isEqualTo("4111111111111111");
    }
}
