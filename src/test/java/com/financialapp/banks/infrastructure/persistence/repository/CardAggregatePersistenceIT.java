package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardAggregatePersistenceIT {

    @Autowired
    CardRepository cardRepository;

    @Test
    void save_then_load_round_trips_installments() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.CREDIT,
                YearMonth.of(2030, 1), new CardBilling(20, 10));
        Card card = Card.create("9999000011112222", new UserId(1L), BankName.GALICIA, details,
                LocalDateTime.now(), LocalDateTime.now());
        card.registerExpense("TV", new Money(new BigDecimal("300.00"), Currency.getInstance("ARS")),
                3, LocalDate.of(2026, 7, 1));

        cardRepository.save(card);

        Card reloaded = cardRepository.findByCardNumber("9999000011112222").orElseThrow();
        assertThat(reloaded.installments()).hasSize(3);
        assertThat(reloaded.installments().get(0).id().value()).isNotNull();
        assertThat(reloaded.installments().get(0).description()).isEqualTo("TV");
    }
}
