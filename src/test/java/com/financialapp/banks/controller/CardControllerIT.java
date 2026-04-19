package com.financialapp.banks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.model.dto.request.BankRequest;
import com.financialapp.banks.model.dto.request.CardRequest;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.model.enums.AccountType;
import com.financialapp.banks.model.enums.CardBehavior;
import com.financialapp.banks.model.enums.CardBrand;
import com.financialapp.banks.model.enums.CardType;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardControllerIT {

    @Autowired WebApplicationContext wac;
    @Autowired ObjectMapper objectMapper;
    @Autowired BankRepository bankRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    void createCard_then_listIncludesIt() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        // 1. Setup Bank and Account
        Bank bank = bankRepository.save(Bank.builder().userId(1L).name("Chase").build());
        Account account = accountRepository.save(Account.builder()
                .bankId(bank.getId()).userId(1L).name("Checking").type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO).currency("USD").isActive(true).build());

        // 2. Create Card
        CardRequest req = new CardRequest(account.getId(), CardBrand.VISA, CardType.PLATINUM,
                CardBehavior.INSTALLMENTS, "1234", LocalDate.now().plusYears(2), 20, 10);

        mockMvc.perform(post("/api/v1/banks/cards")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.last4Digits").value("1234"))
                .andExpect(jsonPath("$.data.displayName").value("Chase VISA PLATINUM ••1234"));

        // 3. List Cards
        mockMvc.perform(get("/api/v1/banks/cards").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].last4Digits").value("1234"));
    }
}
