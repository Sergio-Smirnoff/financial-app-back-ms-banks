package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.web.dto.response.CardResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class CardWebMapperTest {

    private final CardWebMapper mapper = new CardWebMapper();

    @Test
    void toResponse_mapsFieldsAndBuildsDisplayName() {
        // Given a credit card
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.of(2028, 6), new CardBilling(20, 10));
        Card card = Card.create("4111111111111111", new UserId(1L), new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now());

        // When mapped to a response
        CardResponse response = mapper.toResponse(card);

        // Then the fields and the formatted display name are set
        assertThat(response.cardNumber()).isEqualTo("4111111111111111");
        assertThat(response.brand()).isEqualTo(CardBrand.VISA);
        assertThat(response.displayName()).contains("007").contains("VISA").contains("••");
        assertThat(response.closingDay()).isEqualTo(20);
    }

    @Test
    void toResponse_returnsNull_whenCardNull() {
        // Given a null card / When mapped / Then null (the null guard)
        assertThat(mapper.toResponse(null)).isNull();
    }
}
