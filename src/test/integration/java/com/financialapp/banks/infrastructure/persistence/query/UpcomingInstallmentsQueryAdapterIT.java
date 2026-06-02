package com.financialapp.banks.infrastructure.persistence.query;

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
import com.financialapp.banks.domain.query.UpcomingInstallment;
import com.financialapp.banks.domain.query.UpcomingInstallmentsQuery;
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

/** Exercises UpcomingInstallmentsQueryAdapter (both loan + card projections) against H2. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UpcomingInstallmentsQueryAdapterIT {

    @Autowired UpcomingInstallmentsQuery query;
    @Autowired LoanRepository loanRepository;
    @Autowired CardRepository cardRepository;

    private static final UserId USER = new UserId(1L);
    private static final BankNumber BANK = new BankNumber("007");
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDate DUE = LocalDate.of(2026, 6, 15);

    @Test
    void findUnpaidBetween_returnsLoanAndCardRows() {
        // Given a loan and a credit card with installments due inside the window
        loanRepository.save(Loan.originate(USER, BANK, "Car loan",
                new Money(new BigDecimal("1200.00"), ARS), BigDecimal.ZERO, 3,
                AmortizationType.FRENCH, DUE, "0001234567890123456789").loan());

        CardDetails details = new CardDetails(CardBrand.VISA, CardType.STANDARD, CardBehavior.CREDIT,
                YearMonth.now().plusMonths(1), new CardBilling(20, 10));
        List<CardInstallment> installments = CardInstallment.schedule(
                "5555555555554444", "TV", new Money(new BigDecimal("300.00"), ARS), 1, DUE);
        cardRepository.save(new CreditCard(CardNumber.from("5555555555554444"), USER, BANK, details,
                LocalDateTime.now(), LocalDateTime.now(), installments));

        // When querying the unpaid installments due in [DUE, DUE+5d]
        List<UpcomingInstallment> rows = query.findUnpaidBetween(USER, DUE, DUE.plusDays(5));

        // Then both a LOAN and a CARD row are projected
        assertThat(rows).extracting(UpcomingInstallment::type).contains("LOAN", "CARD");
    }
}
