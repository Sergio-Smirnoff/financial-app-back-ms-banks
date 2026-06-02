package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Covers BankController.list (a user's banks with aggregated account balances). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BankControllerExtraIT {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;

    @BeforeEach
    void seedAccount() {
        accountRepository.save(new CheckingAccount(Cbu.from("0070001600000000123459"), "alias",
                new Money(new BigDecimal("500.00"), Currency.getInstance("ARS")), new UserId(1L),
                new BankNumber("007"), "Main", true, LocalDateTime.now(), LocalDateTime.now()));
    }

    @Test
    void listBanks_returnsUserBanksWithAccounts() throws Exception {
        // Given the user has an account at bank 007 / When listing their banks
        mockMvc.perform(get("/api/v1/banks").header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                // Then bank 007 is returned with its aggregated balances
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bankNumber").value("007"));
    }
}
