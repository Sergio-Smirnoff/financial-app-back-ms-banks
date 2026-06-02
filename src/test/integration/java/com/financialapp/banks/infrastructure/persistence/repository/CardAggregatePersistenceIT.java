package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardAggregatePersistenceIT {

    @Autowired CardRepository cardRepository;

    private static final UserId USER = new UserId(1L);
    private static final BankNumber BANK = new BankNumber("007");
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final String CREDIT_PAN = "5555555555554444";
    private static final String DEBIT_PAN = "4111111111111111";

    private CreditCard creditCard() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.CREDIT,
                YearMonth.now().plusMonths(1), new CardBilling(20, 10));
        List<CardInstallment> installments = CardInstallment.schedule(
                CREDIT_PAN, "TV", new Money(new BigDecimal("300.00"), ARS), 3, LocalDate.of(2026, 7, 1));
        return new CreditCard(CardNumber.from(CREDIT_PAN), USER, BANK, details,
                LocalDateTime.now(), LocalDateTime.now(), installments);
    }

    private Card debitCard() {
        CardDetails details = new CardDetails(CardBrand.MASTERCARD, CardType.STANDARD, CardBehavior.INSTANT_PAYMENT,
                YearMonth.now().plusMonths(1), new CardBilling(20, 10));
        return Card.create(DEBIT_PAN, USER, BANK, details, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void save_then_load_roundTripsInstallments() {
        // Given a credit card with a 3-installment schedule / When saved and reloaded
        cardRepository.save(creditCard());
        CreditCard reloaded = (CreditCard) cardRepository.findByCardNumber(CREDIT_PAN).orElseThrow();

        // Then the installments round-trip with generated ids
        assertThat(reloaded.installments()).hasSize(3);
        assertThat(reloaded.installments().get(0).id().value()).isNotNull();
        assertThat(reloaded.installments().get(0).description()).isEqualTo("TV");
    }

    @Test
    void save_debitCard_roundTripsWithoutInstallments() {
        // Given a debit card (exercises the DebitCard branch of the mapper) / When saved and reloaded
        cardRepository.save(debitCard());
        Card reloaded = cardRepository.findByCardNumber(DEBIT_PAN).orElseThrow();

        // Then it is a DebitCard
        assertThat(reloaded).isInstanceOf(DebitCard.class);
    }

    @Test
    void save_existingCard_mergesInPlace() {
        // Given an already-saved card / When saved again (merge path)
        cardRepository.save(creditCard());
        cardRepository.save(creditCard());

        // Then it still resolves to a single reloadable card
        assertThat(cardRepository.findByCardNumber(CREDIT_PAN)).isPresent();
    }

    @Test
    void repositoryQueries_findPersistedCard() {
        // Given a saved credit card
        cardRepository.save(creditCard());

        // When / Then the read projections find it
        assertThat(cardRepository.findByUserId(USER)).isNotEmpty();
        assertThat(cardRepository.findByBankNumber(BANK)).isNotEmpty();
        assertThat(cardRepository.countByBankNumber(BANK)).isPositive();
        assertThat(cardRepository.findByCardNumberAndUserId(CREDIT_PAN, USER)).isPresent();
        assertThat(cardRepository.existsByBankNumberAndBrandAndTypeAndCardNumber(
                BANK, CardBrand.VISA, CardType.STANDARD, CREDIT_PAN)).isTrue();
        assertThat(cardRepository.findExpiringBetween(LocalDate.now(), LocalDate.now().plusDays(60))).isNotEmpty();
    }

    @Test
    void countByBankNumber_isZeroForUnknownBank() {
        // Given an unseeded bank / When counting / Then the orElse(0) branch returns 0
        assertThat(cardRepository.countByBankNumber(new BankNumber("999"))).isZero();
    }

    @Test
    void delete_removesCard() {
        // Given a saved card / When deleted / Then it is gone
        cardRepository.save(creditCard());
        cardRepository.delete(CREDIT_PAN);
        assertThat(cardRepository.findByCardNumber(CREDIT_PAN)).isEmpty();
    }

    @Test
    void save_throwsWhenBankMissing() {
        // Given a card referencing a non-seeded bank / When saved / Then requireBank rejects it
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.CREDIT,
                YearMonth.now().plusMonths(1), new CardBilling(20, 10));
        Card orphan = new CreditCard(CardNumber.from(CREDIT_PAN), USER, new BankNumber("999"), details,
                LocalDateTime.now(), LocalDateTime.now(), List.of());
        assertThatThrownBy(() -> cardRepository.save(orphan)).isInstanceOf(ResourceNotFoundException.class);
    }
}
