package com.financialapp.banks.application.card;

import com.financialapp.banks.domain.usecase.card.command.UpdateCardCommand;
import com.financialapp.banks.application.card.impl.UpdateCardUseCaseImpl;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.*;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
import com.financialapp.banks.domain.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCardUseCaseImplTest {

    @Mock CardRepository cardRepository;
    UpdateCardUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCardUseCaseImpl(cardRepository);
    }

    private CreditCard buildCreditCard(String cardNumber, YearMonth expiry, int closing, int due) {
        CardDetails details = new CardDetails(
                CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                expiry, new CardBilling(closing, due));
        return new CreditCard(CardNumber.from(cardNumber), new UserId(1L), new BankNumber("007"),
                details, LocalDateTime.now(), LocalDateTime.now());
    }

    private DebitCard buildDebitCard(String cardNumber, YearMonth expiry, int closing, int due) {
        CardDetails details = new CardDetails(
                CardBrand.MASTERCARD, CardType.STANDARD, CardBehavior.INSTANT_PAYMENT,
                expiry, new CardBilling(closing, due));
        return new DebitCard(CardNumber.from(cardNumber), new UserId(2L), new BankNumber("072"),
                details, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void shouldUpdateCreditCardBillingAndExpiry() {
        CreditCard existing = buildCreditCard("4111111111111111", YearMonth.of(2026, 1), 15, 10);
        when(cardRepository.findByCardNumberAndUserId(eq("4111111111111111"), any())).thenReturn(Optional.of(existing));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        YearMonth newExpiry = YearMonth.of(2028, 6);
        UpdateCardCommand cmd = new UpdateCardCommand("4111111111111111", new UserId(1L), newExpiry, 20, 5);

        var result = useCase.execute(cmd);

        assertThat(result).isInstanceOf(CreditCard.class);
        assertThat(result.details().expiringDate()).isEqualTo(newExpiry);
        assertThat(result.details().billing().closingDay()).isEqualTo(20);
        assertThat(result.details().billing().dueDay()).isEqualTo(5);
        verify(cardRepository).save(any(CreditCard.class));
    }

    @Test
    void shouldUpdateDebitCardPartially() {
        DebitCard existing = buildDebitCard("4242424242424242", YearMonth.of(2025, 12), 10, 5);
        when(cardRepository.findByCardNumberAndUserId(eq("4242424242424242"), any())).thenReturn(Optional.of(existing));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // only update closingDay, keep expiry and dueDay
        UpdateCardCommand cmd = new UpdateCardCommand("4242424242424242", new UserId(2L), null, 25, null);

        var result = useCase.execute(cmd);

        assertThat(result).isInstanceOf(DebitCard.class);
        assertThat(result.details().expiringDate()).isEqualTo(YearMonth.of(2025, 12));
        assertThat(result.details().billing().closingDay()).isEqualTo(25);
        assertThat(result.details().billing().dueDay()).isEqualTo(5);
    }

    @Test
    void shouldKeepAllFieldsWhenCommandHasNulls() {
        CreditCard existing = buildCreditCard("5555555555554444", YearMonth.of(2027, 3), 12, 8);
        when(cardRepository.findByCardNumberAndUserId(eq("5555555555554444"), any())).thenReturn(Optional.of(existing));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateCardCommand cmd = new UpdateCardCommand("5555555555554444", new UserId(1L), null, null, null);

        var result = useCase.execute(cmd);

        assertThat(result.details().expiringDate()).isEqualTo(YearMonth.of(2027, 3));
        assertThat(result.details().billing().closingDay()).isEqualTo(12);
        assertThat(result.details().billing().dueDay()).isEqualTo(8);
    }

    @Test
    void shouldThrowWhenCardNotFound() {
        when(cardRepository.findByCardNumberAndUserId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new UpdateCardCommand("0000", new UserId(1L), null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("0000");
    }
}
