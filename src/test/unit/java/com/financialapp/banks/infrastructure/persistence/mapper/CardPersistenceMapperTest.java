package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardPersistenceMapperTest {

    private final CardPersistenceMapper mapper = new CardPersistenceMapper();

    private static final Currency USD = Currency.getInstance("USD");
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 0, 0);

    private BankJpaEntity bank() {
        return BankJpaEntity.builder().id(1L).bankNumber("007").name("GALICIA").build();
    }

    @Test
    void toDomain_returnsNull_whenEntityNull() {
        // Given a null entity / When mapped / Then null (the null guard)
        assertThat(mapper.toDomain(null, bank())).isNull();
    }

    @Test
    void toJpa_returnsNull_whenCardNull() {
        // Given null / When mapped / Then null (the null guard)
        assertThat(mapper.toJpa(null, bank())).isNull();
    }

    @Test
    void toJpa_syncsInstallmentWithNullIdObject() {
        // Given a credit card whose installment has a null id object (syncInstallments null branch)
        Money amount = new Money(new BigDecimal("100.00"), USD);
        CardInstallment noId = new CardInstallment(null, "4111111111111111", "Mac", amount, 1, 1,
                amount, LocalDate.of(2026, 6, 1), false, null, T0, T0);
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.CREDIT,
                YearMonth.of(2030, 1), new CardBilling(20, 10));
        CreditCard card = new CreditCard(CardNumber.from("4111111111111111"), new UserId(1L),
                new BankNumber("007"), details, T0, T0, List.of(noId));

        // When mapped to a JPA entity
        CardJpaEntity entity = mapper.toJpa(card, bank());

        // Then the child installment is present with a null id
        assertThat(entity.getInstallments()).hasSize(1);
        assertThat(entity.getInstallments().get(0).getId()).isNull();
    }
}
