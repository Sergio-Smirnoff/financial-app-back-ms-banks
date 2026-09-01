package com.financialapp.banks.application.card;

import com.financialapp.banks.domain.usecase.card.command.RegisterCardExpenseCommand;
import com.financialapp.banks.application.card.impl.RegisterCardExpenseUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.card.CardInstallmentNotSupportedException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterCardExpenseUseCaseImplTest {

    @Mock CardRepository cardRepository;
    RegisterCardExpenseUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterCardExpenseUseCaseImpl(cardRepository);
    }

    private Card creditCard() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM,
                CardBehavior.CREDIT, YearMonth.now().plusYears(2), new CardBilling(20, 10), null);
        return Card.create("4111111111111111", new UserId(1L), new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void create_generatesMultipleInstallments() {
        when(cardRepository.findByCardNumberAndUserId("1234", new UserId(1L))).thenReturn(Optional.of(creditCard()));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        List<CardInstallment> result = useCase.execute(new RegisterCardExpenseCommand(
                "1234", new UserId(1L), "New Mac",
                new Money(new BigDecimal("3000"), Currency.getInstance("USD")),
                3, LocalDate.of(2026, 5, 1)));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).amount().amount()).isEqualByComparingTo("1000.00");
        assertThat(result.get(0).dueDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(result.get(2).dueDate()).isEqualTo(LocalDate.of(2026, 7, 1));
    }

    @Test
    void create_rejectsInstantPaymentCard() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD,
                CardBehavior.INSTANT_PAYMENT, YearMonth.now().plusYears(2), new CardBilling(20, 10), null);
        Card debit = Card.create("4111111111111111", new UserId(1L), new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now());
        when(cardRepository.findByCardNumberAndUserId("1234", new UserId(1L))).thenReturn(Optional.of(debit));

        assertThatThrownBy(() -> useCase.execute(new RegisterCardExpenseCommand(
                "1234", new UserId(1L), "Coffee",
                new Money(BigDecimal.TEN, Currency.getInstance("USD")), 1, LocalDate.now())))
                .isInstanceOf(CardInstallmentNotSupportedException.class);
    }
}
