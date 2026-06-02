package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UpcomingPaymentControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired LoanRepository loanRepository;

    @Test
    void getUpcomingPayments_returnsLoanInstallmentsInWindow() throws Exception {
        // Given a loan whose first installment falls inside the queried window
        LocalDate start = LocalDate.of(2026, 6, 15);
        loanRepository.save(Loan.originate(new UserId(1L), new BankNumber("007"), "Car loan",
                new Money(new BigDecimal("1200.00"), Currency.getInstance("ARS")), BigDecimal.ZERO, 12,
                AmortizationType.FRENCH, start, "0001234567890123456789").loan());

        // When querying upcoming payments over [start, start+5d]
        mockMvc.perform(get("/api/v1/banks/upcoming-payments")
                        .param("from", start.toString())
                        .param("to", start.plusDays(5).toString())
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                // Then a LOAN row is returned
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("LOAN"));
    }
}
