package com.financialapp.banks.infrastructure.persistence.repository;

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
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
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

/** Exercises the folded upcoming-installment read methods on Loan/Card repositories against H2. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UpcomingInstallmentsRepositoryIT {

    @Autowired LoanRepository loanRepository;
    @Autowired CardRepository cardRepository;

    private static final UserId USER = new UserId(1L);
    private static final BankNumber BANK = new BankNumber("007");
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDate DUE = LocalDate.of(2026, 6, 15);

    private void seedLoanAndCard() {
        loanRepository.save(Loan.originate(USER, BANK, "Car loan",
                new Money(new BigDecimal("1200.00"), ARS), BigDecimal.ZERO, 3,
                AmortizationType.FRENCH, DUE, "0001234567890123456789").loan());

        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.CREDIT,
                YearMonth.now().plusMonths(1), new CardBilling(20, 10));
        List<CardInstallment> installments = CardInstallment.schedule(
                "5555555555554444", "TV", new Money(new BigDecimal("300.00"), ARS), 1, DUE);
        cardRepository.save(new CreditCard(CardNumber.from("5555555555554444"), USER, BANK, details,
                LocalDateTime.now(), LocalDateTime.now(), installments));
    }

    @Test
    void loanRepository_findsLoansWithUpcomingUnpaidInstallments() {
        // Given a loan whose first installment falls inside the window
        seedLoanAndCard();

        // When querying loans with upcoming unpaid installments in [DUE, DUE+5d]
        List<Loan> loans = loanRepository.findWithUpcomingUnpaidInstallments(USER, DUE, DUE.plusDays(5));

        // Then the loan is returned with its (full) installment schedule loaded
        assertThat(loans).hasSize(1);
        assertThat(loans.get(0).name()).isEqualTo("Car loan");
        assertThat(loans.get(0).installments()).anyMatch(i -> i.dueDate().equals(DUE) && !i.paid());
    }

    @Test
    void cardRepository_findsUpcomingUnpaidCardInstallments() {
        // Given a credit card with an installment due inside the window
        seedLoanAndCard();

        // When querying upcoming unpaid card installments in [DUE, DUE+5d]
        List<CardInstallment> rows = cardRepository.findUpcomingUnpaidInstallments(USER, DUE, DUE.plusDays(5));

        // Then the card installment is projected
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).description()).isEqualTo("TV");
        assertThat(rows.get(0).paid()).isFalse();
    }

    @Test
    void returnsEmpty_whenNothingDueInWindow() {
        // Given seeded data due in June / When querying a far-future window
        seedLoanAndCard();
        LocalDate far = DUE.plusYears(5);

        // Then neither repository reports upcoming installments
        assertThat(loanRepository.findWithUpcomingUnpaidInstallments(USER, far, far.plusDays(5))).isEmpty();
        assertThat(cardRepository.findUpcomingUnpaidInstallments(USER, far, far.plusDays(5))).isEmpty();
    }
}
