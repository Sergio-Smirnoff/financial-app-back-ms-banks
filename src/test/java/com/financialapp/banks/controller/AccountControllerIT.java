package com.financialapp.banks.controller;

import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.model.enums.AccountType;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired BankRepository bankRepository;

    Account account;

    @BeforeEach
    void setUp() {
        Bank bank = bankRepository.save(Bank.builder().userId(1L).name("Test Bank").build());
        account = accountRepository.save(Account.builder()
                .bankId(bank.getId())
                .userId(1L)
                .name("Savings")
                .type(AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .isActive(true)
                .build());
    }

    @Test
    void adjustBalance_updatesBalance() throws Exception {
        mockMvc.perform(patch("/api/v1/banks/accounts/{id}/balance/adjust", account.getId())
                .param("delta", "500.50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo("1500.50");
    }

    @Test
    void adjustBalance_returnsNotFoundForInvalidId() throws Exception {
        mockMvc.perform(patch("/api/v1/banks/accounts/{id}/balance/adjust", 9999L)
                .param("delta", "100.00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
