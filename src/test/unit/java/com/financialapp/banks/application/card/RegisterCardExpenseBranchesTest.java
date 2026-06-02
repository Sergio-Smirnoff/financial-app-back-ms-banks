package com.financialapp.banks.application.card;

import com.financialapp.banks.application.card.impl.RegisterCardExpenseUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.usecase.card.command.RegisterCardExpenseCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Branch coverage for RegisterCardExpense: card-not-found and the result filter's false branches. */
@ExtendWith(MockitoExtension.class)
class RegisterCardExpenseBranchesTest {

    @Mock CardRepository cardRepository;
    RegisterCardExpenseUseCaseImpl useCase;

    private static final UserId USER = new UserId(1L);
    private static final String PAN = "4111111111111111";
    private static final Currency USD = Currency.getInstance("USD");
    private static final LocalDate D = LocalDate.of(2026, 6, 1);

    @BeforeEach
    void setUp() {
        useCase = new RegisterCardExpenseUseCaseImpl(cardRepository);
    }

    @Test
    void throwsWhenCardMissing() {
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(new RegisterCardExpenseCommand(
                "c", USER, "X", new Money(BigDecimal.TEN, USD), 1, D)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnsOnlyNewlyAddedInstallments_excludingPreexisting() {
        // Pre-existing installments: a different description, and a same-name one with an earlier due date.
        List<CardInstallment> preexisting = new ArrayList<>();
        preexisting.addAll(CardInstallment.schedule(PAN, "Old", new Money(new BigDecimal("50.00"), USD), 1, D));
        preexisting.addAll(CardInstallment.schedule(PAN, "New", new Money(new BigDecimal("50.00"), USD), 1, D.minusMonths(1)));
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
        CreditCard card = new CreditCard(CardNumber.from(PAN), USER, new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now(), preexisting);

        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CardInstallment> result = useCase.execute(new RegisterCardExpenseCommand(
                "c", USER, "New", new Money(new BigDecimal("100.00"), USD), 1, D));

        // Only the just-added "New" installment dated D is returned (Old excluded by name,
        // earlier "New" excluded by due date).
        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("New");
        assertThat(result.get(0).dueDate()).isEqualTo(D);
    }
}
