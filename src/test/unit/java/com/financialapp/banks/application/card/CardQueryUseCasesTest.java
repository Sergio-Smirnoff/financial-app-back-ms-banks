package com.financialapp.banks.application.card;

import com.financialapp.banks.application.card.impl.CancelCardUseCaseImpl;
import com.financialapp.banks.application.card.impl.CheckDuplicateExpensesUseCaseImpl;
import com.financialapp.banks.application.card.impl.ListCardInstallmentsUseCaseImpl;
import com.financialapp.banks.application.card.impl.ListCardsUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceConflictException;
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
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.usecase.card.command.CancelCardCommand;
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
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for the read/cancel card use cases (one impl per @Test group). */
@ExtendWith(MockitoExtension.class)
class CardQueryUseCasesTest {

    @Mock CardRepository cardRepository;
    @Mock BankRepository bankRepository;

    private static final UserId USER = new UserId(1L);
    private static final Currency USD = Currency.getInstance("USD");
    private static final String PAN = "4111111111111111";
    private static final LocalDate DUE = LocalDate.of(2026, 6, 1);

    private CreditCard credit(List<CardInstallment> installments) {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
        return new CreditCard(CardNumber.from(PAN), USER, new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now(), installments);
    }

    private Card debit() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.INSTANT_PAYMENT,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
        return Card.create(PAN, USER, new BankNumber("007"), details, LocalDateTime.now(), LocalDateTime.now());
    }

    private List<CardInstallment> oneInstallment() {
        return CardInstallment.schedule(PAN, "Coffee", new Money(new BigDecimal("100.00"), USD), 1, DUE);
    }

    // --- CancelCard ---

    @Test
    void cancelCard_throwsWhenMissing() {
        var useCase = new CancelCardUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(new CancelCardCommand("c", USER)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(cardRepository, never()).delete("c");
    }

    @Test
    void cancelCard_rejectsCreditWithUnpaidInstallments() {
        var useCase = new CancelCardUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(credit(oneInstallment())));
        assertThatThrownBy(() -> useCase.execute(new CancelCardCommand("c", USER)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("unpaid installments");
        verify(cardRepository, never()).delete("c");
    }

    @Test
    void cancelCard_deletesCreditWhenAllPaid() {
        var useCase = new CancelCardUseCaseImpl(cardRepository);
        List<CardInstallment> paid = oneInstallment().stream().map(i -> i.pay(DUE)).toList();
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(credit(paid)));
        useCase.execute(new CancelCardCommand("c", USER));
        verify(cardRepository).delete("c");
    }

    @Test
    void cancelCard_deletesDebitCard() {
        var useCase = new CancelCardUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(debit()));
        useCase.execute(new CancelCardCommand("c", USER));
        verify(cardRepository).delete("c");
    }

    // --- CheckDuplicateExpenses ---

    @Test
    void checkDuplicates_throwsWhenMissing() {
        var useCase = new CheckDuplicateExpensesUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute("c", USER, List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void checkDuplicates_returnsEmptyForNonCreditCard() {
        var useCase = new CheckDuplicateExpensesUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(debit()));
        assertThat(useCase.execute("c", USER, List.of())).isEmpty();
    }

    @Test
    void checkDuplicates_returnsIndicesOfMatchingExpenses() {
        var useCase = new CheckDuplicateExpensesUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(credit(oneInstallment())));

        var duplicate = new RegisterCardExpenseCommand("c", USER, "Coffee",
                new Money(new BigDecimal("100.00"), USD), 1, DUE);
        var fresh = new RegisterCardExpenseCommand("c", USER, "Tea",
                new Money(new BigDecimal("100.00"), USD), 1, DUE);

        assertThat(useCase.execute("c", USER, List.of(duplicate, fresh))).containsExactly(0);
    }

    @Test
    void checkDuplicates_sameDescriptionDifferentAmount_isNotDuplicate() {
        var useCase = new CheckDuplicateExpensesUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(credit(oneInstallment())));

        var sameNameOtherAmount = new RegisterCardExpenseCommand("c", USER, "Coffee",
                new Money(new BigDecimal("999.00"), USD), 1, DUE);

        assertThat(useCase.execute("c", USER, List.of(sameNameOtherAmount))).isEmpty();
    }

    @Test
    void checkDuplicates_sameAmountDifferentDueDate_isNotDuplicate() {
        var useCase = new CheckDuplicateExpensesUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(credit(oneInstallment())));

        var sameButOtherDate = new RegisterCardExpenseCommand("c", USER, "Coffee",
                new Money(new BigDecimal("100.00"), USD), 1, DUE.plusMonths(1));

        assertThat(useCase.execute("c", USER, List.of(sameButOtherDate))).isEmpty();
    }

    // --- ListCardInstallments ---

    @Test
    void listInstallments_throwsWhenMissing() {
        var useCase = new ListCardInstallmentsUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute("c", USER)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listInstallments_returnsInstallmentsForCredit() {
        var useCase = new ListCardInstallmentsUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(credit(oneInstallment())));
        assertThat(useCase.execute("c", USER)).hasSize(1);
    }

    @Test
    void listInstallments_returnsEmptyForDebit() {
        var useCase = new ListCardInstallmentsUseCaseImpl(cardRepository);
        when(cardRepository.findByCardNumberAndUserId("c", USER)).thenReturn(Optional.of(debit()));
        assertThat(useCase.execute("c", USER)).isEmpty();
    }

    // --- ListCards ---

    @Test
    void listCards_byBankNumber_whenBankFound() {
        var useCase = new ListCardsUseCaseImpl(cardRepository, bankRepository);
        var bank = new BankNumber("007");
        when(bankRepository.findByBankNumber(bank))
                .thenReturn(Optional.of(new com.financialapp.banks.domain.model.bank.Bank(bank, "GALICIA", null)));
        when(cardRepository.findByBankNumber(bank)).thenReturn(List.of(debit()));

        assertThat(useCase.execute(USER, bank)).hasSize(1);
        verify(cardRepository, never()).findByUserId(USER);
    }

    @Test
    void listCards_throwsWhenBankMissing() {
        var useCase = new ListCardsUseCaseImpl(cardRepository, bankRepository);
        var bank = new BankNumber("007");
        when(bankRepository.findByBankNumber(bank)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(USER, bank)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listCards_byUser_whenBankNull() {
        var useCase = new ListCardsUseCaseImpl(cardRepository, bankRepository);
        when(cardRepository.findByUserId(USER)).thenReturn(List.of(debit()));
        assertThat(useCase.execute(USER, null)).hasSize(1);
    }
}
