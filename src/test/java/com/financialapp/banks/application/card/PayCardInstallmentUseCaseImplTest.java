package com.financialapp.banks.application.card;

import com.financialapp.banks.application.card.impl.PayCardInstallmentUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.CardInstallmentPaidEvent;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.card.CardInstallmentAlreadyPaidException;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.card.command.PayCardInstallmentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayCardInstallmentUseCaseImplTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final String CARD = "1234567890123456";

    @Mock CardRepository cardRepository;
    @Mock AdjustBalanceUseCase adjustBalance;
    @Mock DomainEventPublisher eventPublisher;
    PayCardInstallmentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new PayCardInstallmentUseCaseImpl(cardRepository, adjustBalance, eventPublisher);
    }

    private CardInstallment installment(boolean paid) {
        return new CardInstallment(new CardInstallmentId(20L), CARD, "New Mac",
                new Money(new BigDecimal("3000.00"), USD), 2, 3,
                new Money(new BigDecimal("1000.00"), USD), LocalDate.of(2026, 6, 1),
                paid, paid ? LocalDate.of(2026, 5, 1) : null,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    /** Credit card carrying installment id=20 in the given paid state (or no installments). */
    private Card card(boolean withInstallment, boolean paid) {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM,
                CardBehavior.CREDIT, YearMonth.now().plusYears(2), new CardBilling(20, 10));
        Card card = Card.create(CARD, new UserId(7L), BankName.GALICIA, details,
                LocalDateTime.now(), LocalDateTime.now());
        if (withInstallment) {
            card.restoreInstallments(List.of(installment(paid)));
        }
        return card;
    }

    private PayCardInstallmentCommand cmd(LocalDate paidDate) {
        return new PayCardInstallmentCommand(CARD, new CardInstallmentId(20L),
                new UserId(7L), "1234567890123456789012", paidDate);
    }

    @Test
    void pay_debitsSavesAndPublishesEvent() {
        LocalDate paidDate = LocalDate.of(2026, 5, 2);
        when(cardRepository.findByCardNumberAndUserId(CARD, new UserId(7L)))
                .thenReturn(Optional.of(card(true, false)));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardInstallment result = useCase.execute(cmd(paidDate));

        assertThat(result.paid()).isTrue();
        assertThat(result.paidDate()).isEqualTo(paidDate);

        ArgumentCaptor<AdjustBalanceCommand> adjustCaptor = ArgumentCaptor.forClass(AdjustBalanceCommand.class);
        verify(adjustBalance).execute(adjustCaptor.capture());
        assertThat(adjustCaptor.getValue().delta().amount()).isEqualByComparingTo("-1000.00");

        ArgumentCaptor<CardInstallmentPaidEvent> eventCaptor = ArgumentCaptor.forClass(CardInstallmentPaidEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        CardInstallmentPaidEvent event = eventCaptor.getValue();
        assertThat(event.amount().amount()).isEqualByComparingTo("-1000.00");
        assertThat(event.installmentNumber()).isEqualTo(2);
        assertThat(event.totalInstallments()).isEqualTo(3);
        assertThat(event.paidDate()).isEqualTo(paidDate);
    }

    @Test
    void pay_defaultsPaidDateToToday() {
        when(cardRepository.findByCardNumberAndUserId(CARD, new UserId(7L)))
                .thenReturn(Optional.of(card(true, false)));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardInstallment result = useCase.execute(cmd(null));

        assertThat(result.paidDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void pay_throwsWhenCardMissing() {
        when(cardRepository.findByCardNumberAndUserId(CARD, new UserId(7L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(cmd(LocalDate.now())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(adjustBalance, never()).execute(any());
    }

    @Test
    void pay_throwsWhenInstallmentNotOnCard() {
        when(cardRepository.findByCardNumberAndUserId(CARD, new UserId(7L)))
                .thenReturn(Optional.of(card(false, false)));

        assertThatThrownBy(() -> useCase.execute(cmd(LocalDate.now())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(adjustBalance, never()).execute(any());
    }

    @Test
    void pay_throwsWhenAlreadyPaid_andDoesNotDebit() {
        when(cardRepository.findByCardNumberAndUserId(CARD, new UserId(7L)))
                .thenReturn(Optional.of(card(true, true)));

        assertThatThrownBy(() -> useCase.execute(cmd(LocalDate.now())))
                .isInstanceOf(CardInstallmentAlreadyPaidException.class);
        verify(adjustBalance, never()).execute(any());
        verify(cardRepository, never()).save(any());
    }
}
