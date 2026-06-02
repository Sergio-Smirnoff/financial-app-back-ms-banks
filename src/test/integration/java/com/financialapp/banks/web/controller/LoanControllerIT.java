package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.web.dto.request.LoanRequest;
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
import java.util.Currency;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;

    private static final String CBU = "0070001600000000123459";

    @BeforeEach
    void seedDepositAccount() {
        accountRepository.save(new CheckingAccount(Cbu.from(CBU), "alias",
                new Money(BigDecimal.ZERO, Currency.getInstance("ARS")), new UserId(1L), new BankNumber("007"),
                "Deposit", true, LocalDateTime.now(), LocalDateTime.now()));
    }

    private LoanRequest loanRequest() {
        return new LoanRequest("007", CBU, "Car loan", "1200.00", "0", 12, LocalDate.of(2026, 6, 1));
    }

    private long createLoanAndReturnId() throws Exception {
        String body = mockMvc.perform(post("/api/v1/banks/loans")
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    @Test
    void create_then_listIncludesLoan() throws Exception {
        // Given a created loan / When listing / Then it appears
        createLoanAndReturnId();
        mockMvc.perform(get("/api/v1/banks/loans").header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Car loan"));
    }

    @Test
    void list_filteredByBankNumber() throws Exception {
        // Given a created loan / When listing filtered by its bank
        createLoanAndReturnId();
        mockMvc.perform(get("/api/v1/banks/loans").param("bankNumber", "007")
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bankNumber").value("007"));
    }

    @Test
    void getInstallments_returnsFullSchedule() throws Exception {
        // Given a created loan / When fetching its installments / Then the 12-entry schedule returns
        long id = createLoanAndReturnId();
        mockMvc.perform(get("/api/v1/banks/loans/{id}/installments", id)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(12));
    }

    @Test
    void payInstallment_marksItPaid() throws Exception {
        // Given a created loan and its first installment id
        long id = createLoanAndReturnId();
        String insts = mockMvc.perform(get("/api/v1/banks/loans/{id}/installments", id)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andReturn().getResponse().getContentAsString();
        long installmentId = ((Number) JsonPath.read(insts, "$.data[0].id")).longValue();

        // When paying it from the deposit account / Then it is marked paid
        mockMvc.perform(post("/api/v1/banks/loans/{id}/installments/{installmentId}/pay", id, installmentId)
                        .param("accountCbu", CBU)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paid").value(true));
    }

    @Test
    void delete_removesLoan() throws Exception {
        // Given a created loan / When deleted / Then OK
        long id = createLoanAndReturnId();
        mockMvc.perform(delete("/api/v1/banks/loans/{id}", id)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void getLoanInstallments_notFound_returns404WithCode() throws Exception {
        // Given no such loan / When fetching installments / Then 404
        mockMvc.perform(get("/api/v1/banks/loans/{id}/installments", 99999L)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("resource_not_found"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
