package com.financialapp.banks.application.card;

import com.financialapp.banks.application.card.impl.PayCardInstallmentUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.card.CardInstallmentNotSupportedException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.card.command.PayCardInstallmentCommand;
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
import static org.mockito.Mockito.when;

/** Branch coverage for PayCardInstallment: non-credit card, mid-list match, and not-found with a null installment id. */
@ExtendWith(MockitoExtension.class)
class PayCardInstallmentBranchesTest {

    @Mock CardRepository cardRepository;
    @Mock AdjustBalanceUseCase adjustBalance;
    @Mock DomainEventPublisher eventPublisher;
    PayCardInstallmentUseCaseImpl useCase;

    private static final UserId USER = new UserId(1L);
    private static final String PAN = "4111111111111111";
    private static final Currency USD = Currency.getInstance("USD");
    private static final LocalDate DUE = LocalDate.of(2026, 6, 1);

    @BeforeEach
    void setUp() {
        useCase = new PayCardInstallmentUseCaseImpl(cardRepository, adjustBalance, eventPublisher);
    }

    private CardDetails details(CardBehavior behavior) {
        return new CardDetails(CardBrand.VISA, CardType.PLATINUM, behavior,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
    }

    private CardInstallment installment(long id, int number) {
        Money amount = new Money(new BigDecimal("100.00"), USD);
        return new CardInstallment(new CardInstallmentId(id), PAN, "exp " + number, amount, number, 2,
                amount, DUE, false, null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void rejectsNonCreditCard() {
        // Given a debit card (not a CreditCard)
        Card debit = Card.create(PAN, USER, new BankNumber("007"), details(CardBehavior.INSTANT_PAYMENT),
                LocalDateTime.now(), LocalDateTime.now());
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(debit));

        // When paying an installment on it / Then it is not supported
        assertThatThrownBy(() -> useCase.execute(new PayCardInstallmentCommand(
                "c", new CardInstallmentId(1L), USER, "cbu", LocalDate.now())))
                .isInstanceOf(CardInstallmentNotSupportedException.class);
    }

    @Test
    void paysSecondInstallment_skippingNonMatchingFirst() {
        // Given a credit card with two installments carrying distinct ids
        CreditCard credit = new CreditCard(CardNumber.from(PAN), USER, new BankNumber("007"),
                details(CardBehavior.CREDIT), LocalDateTime.now(), LocalDateTime.now(),
                List.of(installment(1L, 1), installment(2L, 2)));
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(credit));

        // When paying the second installment (first is skipped in the match loop)
        CardInstallment paid = useCase.execute(new PayCardInstallmentCommand(
                "c", new CardInstallmentId(2L), USER, "cbu", DUE));

        // Then the second installment is the one marked paid
        assertThat(paid.installmentNumber()).isEqualTo(2);
        assertThat(paid.paid()).isTrue();
    }

    @Test
    void throwsNotFoundWithNewLiteral_whenInstallmentIdNullAndAbsent() {
        // Given a credit card with no installments
        CreditCard credit = new CreditCard(CardNumber.from(PAN), USER, new BankNumber("007"),
                details(CardBehavior.CREDIT), LocalDateTime.now(), LocalDateTime.now(), List.of());
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(credit));

        // When paying with a null installment id / Then not-found renders the id as "new"
        assertThatThrownBy(() -> useCase.execute(new PayCardInstallmentCommand(
                "c", new CardInstallmentId(null), USER, "cbu", LocalDate.now())))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("new");
    }
}
