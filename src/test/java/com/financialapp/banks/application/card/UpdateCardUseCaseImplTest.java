package com.financialapp.banks.application.card;

import com.financialapp.banks.application.card.command.UpdateCardCommand;
import com.financialapp.banks.application.card.impl.UpdateCardUseCaseImpl;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankName;
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
        return new CreditCard(CardNumber.of(cardNumber), new UserId(1L), BankName.GALICIA,
                details, LocalDateTime.now(), LocalDateTime.now());
    }

    private DebitCard buildDebitCard(String cardNumber, YearMonth expiry, int closing, int due) {
        CardDetails details = new CardDetails(
                CardBrand.MASTERCARD, CardType.STANDARD, CardBehavior.INSTANT_PAYMENT,
                expiry, new CardBilling(closing, due));
        return new DebitCard(CardNumber.of(cardNumber), new UserId(2L), BankName.SANTANDER,
                details, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void shouldUpdateCreditCardBillingAndExpiry() {
        CreditCard existing = buildCreditCard("1234567890123456", YearMonth.of(2026, 1), 15, 10);
        when(cardRepository.findByCardNumberAndUserId(eq("1234567890123456"), any())).thenReturn(Optional.of(existing));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        YearMonth newExpiry = YearMonth.of(2028, 6);
        UpdateCardCommand cmd = new UpdateCardCommand("1234567890123456", new UserId(1L), newExpiry, 20, 5);

        var result = useCase.execute(cmd);

        assertThat(result).isInstanceOf(CreditCard.class);
        assertThat(result.details().expiringDate()).isEqualTo(newExpiry);
        assertThat(result.details().billing().closingDay()).isEqualTo(20);
        assertThat(result.details().billing().dueDay()).isEqualTo(5);
        verify(cardRepository).save(any(CreditCard.class));
    }

    @Test
    void shouldUpdateDebitCardPartially() {
        DebitCard existing = buildDebitCard("5678901234567890", YearMonth.of(2025, 12), 10, 5);
        when(cardRepository.findByCardNumberAndUserId(eq("5678901234567890"), any())).thenReturn(Optional.of(existing));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // only update closingDay, keep expiry and dueDay
        UpdateCardCommand cmd = new UpdateCardCommand("5678901234567890", new UserId(2L), null, 25, null);

        var result = useCase.execute(cmd);

        assertThat(result).isInstanceOf(DebitCard.class);
        assertThat(result.details().expiringDate()).isEqualTo(YearMonth.of(2025, 12));
        assertThat(result.details().billing().closingDay()).isEqualTo(25);
        assertThat(result.details().billing().dueDay()).isEqualTo(5);
    }

    @Test
    void shouldKeepAllFieldsWhenCommandHasNulls() {
        CreditCard existing = buildCreditCard("9999999999999999", YearMonth.of(2027, 3), 12, 8);
        when(cardRepository.findByCardNumberAndUserId(eq("9999999999999999"), any())).thenReturn(Optional.of(existing));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateCardCommand cmd = new UpdateCardCommand("9999999999999999", new UserId(1L), null, null, null);

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
