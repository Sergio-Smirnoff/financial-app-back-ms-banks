package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.web.dto.request.CardExpenseCreateRequest;
import com.financialapp.banks.web.dto.request.CardExpenseImportRequest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CardInstallmentControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CardRepository cardRepository;
    @Autowired AccountRepository accountRepository;

    private static final String PAN = "4111111111111111";
    private static final String CBU = "0070001600000000123459";

    @BeforeEach
    void seedCardAndAccount() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
        cardRepository.save(new CreditCard(CardNumber.from(PAN), new UserId(1L), new BankNumber("007"),
                details, LocalDateTime.now(), LocalDateTime.now(), List.of()));
        accountRepository.save(new CheckingAccount(Cbu.from(CBU), "alias",
                new Money(new BigDecimal("100000.00"), Currency.getInstance("ARS")), new UserId(1L),
                new BankNumber("007"), "Deposit", true, LocalDateTime.now(), LocalDateTime.now()));
    }

    private void header(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder b) {
        b.header("X-User-Id", "1").header("X-Internal-Token", "test-token");
    }

    private long createExpenseReturnFirstInstallmentId() throws Exception {
        var req = new CardExpenseCreateRequest("TV", "300.00", "ARS", 3, LocalDate.of(2026, 7, 1));
        var rb = post("/api/v1/banks/cards/{c}/installments", PAN)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req));
        header(rb);
        String body = mockMvc.perform(rb)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data[0].id")).longValue();
    }

    @Test
    void createExpense_then_listShowsInstallments() throws Exception {
        // Given an expense split into installments / When listing / Then they show
        createExpenseReturnFirstInstallmentId();
        var rb = get("/api/v1/banks/cards/{c}/installments", PAN);
        header(rb);
        mockMvc.perform(rb).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void payInstallment_marksItPaid() throws Exception {
        // Given a created installment / When paying from the deposit account / Then it is paid
        long installmentId = createExpenseReturnFirstInstallmentId();
        var rb = post("/api/v1/banks/cards/{c}/installments/{id}/pay", PAN, installmentId).param("accountCbu", CBU);
        header(rb);
        mockMvc.perform(rb).andExpect(status().isOk()).andExpect(jsonPath("$.data.paid").value(true));
    }

    @Test
    void importExpenses_returnsBatchResult() throws Exception {
        // Given an import with one ARS expense / When importing / Then it is counted as imported
        var imported = new CardExpenseImportRequest.ImportedExpense("Groceries", "150.00", "ARS", LocalDate.of(2026, 7, 1));
        var req = new CardExpenseImportRequest(CBU, "0070000000000000000099", List.of(imported));
        var rb = post("/api/v1/banks/cards/{c}/installments/import", PAN)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req));
        header(rb);
        mockMvc.perform(rb).andExpect(status().isOk()).andExpect(jsonPath("$.data.imported").value(1));
    }

    @Test
    void checkDuplicates_returnsIndices() throws Exception {
        // Given an existing installment and a duplicate candidate / When checking / Then index 0 is flagged
        createExpenseReturnFirstInstallmentId();
        var duplicate = new CardExpenseCreateRequest("TV", "100.00", "ARS", 1, LocalDate.of(2026, 7, 1));
        var rb = post("/api/v1/banks/cards/{c}/installments/duplicates-check", PAN)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(List.of(duplicate)));
        header(rb);
        mockMvc.perform(rb).andExpect(status().isOk()).andExpect(jsonPath("$.data[0]").value(0));
    }
}
