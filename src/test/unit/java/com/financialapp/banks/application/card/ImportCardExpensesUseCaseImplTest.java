package com.financialapp.banks.application.card;

import com.financialapp.banks.application.card.impl.ImportCardExpensesUseCaseImpl;
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
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.usecase.card.BatchImportResult;
import com.financialapp.banks.domain.usecase.card.PayCardInstallmentUseCase;
import com.financialapp.banks.domain.usecase.card.RegisterCardExpenseUseCase;
import com.financialapp.banks.domain.usecase.card.command.ImportCardExpensesCommand;
import com.financialapp.banks.domain.usecase.card.command.ImportCardExpensesCommand.ImportedExpense;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportCardExpensesUseCaseImplTest {

    @Mock CardRepository cardRepository;
    @Mock RegisterCardExpenseUseCase createExpense;
    @Mock PayCardInstallmentUseCase payInstallment;
    ImportCardExpensesUseCaseImpl useCase;

    private static final UserId USER = new UserId(1L);
    private static final String PAN = "4111111111111111";
    private static final LocalDate DATE = LocalDate.of(2026, 5, 1);

    @BeforeEach
    void setUp() {
        useCase = new ImportCardExpensesUseCaseImpl(cardRepository, createExpense, payInstallment);
    }

    private Card anyCard() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
        return Card.create(PAN, USER, new BankNumber("007"), details, LocalDateTime.now(), LocalDateTime.now());
    }

    private ImportCardExpensesCommand command(ImportedExpense... expenses) {
        return new ImportCardExpensesCommand(PAN, USER, "0070000000000000000001", "0070000000000000000002", List.of(expenses));
    }

    private ImportedExpense expense(String desc, String currency) {
        return new ImportedExpense(desc, new Money(new BigDecimal("100.00"), Currency.getInstance(currency)), DATE);
    }

    @Test
    void throwsWhenCardMissing() {
        when(cardRepository.findByCardNumberAndUserId(PAN, USER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(command(expense("Coffee", "ARS"))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void skipsExpenseWithUnsupportedCurrency() {
        when(cardRepository.findByCardNumberAndUserId(PAN, USER)).thenReturn(Optional.of(anyCard()));

        BatchImportResult result = useCase.execute(command(expense("Paris", "EUR")));

        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        verify(createExpense, never()).execute(any());
    }

    @Test
    void importsArsExpense() {
        when(cardRepository.findByCardNumberAndUserId(PAN, USER)).thenReturn(Optional.of(anyCard()));
        when(createExpense.execute(any())).thenReturn(
                CardInstallment.schedule(PAN, "Coffee", new Money(new BigDecimal("100.00"), Currency.getInstance("ARS")), 1, DATE));

        BatchImportResult result = useCase.execute(command(expense("Coffee", "ARS")));

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        verify(payInstallment).execute(any());
    }

    @Test
    void importsUsdExpense() {
        when(cardRepository.findByCardNumberAndUserId(PAN, USER)).thenReturn(Optional.of(anyCard()));
        when(createExpense.execute(any())).thenReturn(
                CardInstallment.schedule(PAN, "Mac", new Money(new BigDecimal("100.00"), Currency.getInstance("USD")), 1, DATE));

        BatchImportResult result = useCase.execute(command(expense("Mac", "USD")));

        assertThat(result.imported()).isEqualTo(1);
    }

    @Test
    void recordsErrorWhenRegistrationFails() {
        when(cardRepository.findByCardNumberAndUserId(PAN, USER)).thenReturn(Optional.of(anyCard()));
        when(createExpense.execute(any())).thenThrow(new RuntimeException("boom"));

        BatchImportResult result = useCase.execute(command(expense("Coffee", "ARS")));

        assertThat(result.imported()).isZero();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).contains("Coffee").contains("boom");
    }
}
