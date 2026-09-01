package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.*;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.web.dto.request.AccountFeeScheduleRequest;
import com.financialapp.banks.web.dto.request.CardFeeScheduleRequest;
import com.financialapp.commons.core.domain.model.IvaTreatment;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FeeControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CardRepository cardRepository;

    private static final Cbu CBU = Cbu.from("0070001600000000123459");
    private static final String CARD_NUM = "4111111111111111";

    @BeforeEach
    void setUp() {
        UserId userId = new UserId(1L);
        Account account = Account.create(AccountType.CHECKING, CBU, "alias", new Money(BigDecimal.TEN, Currency.getInstance("ARS")), userId, new BankNumber("007"), "Account", true, LocalDateTime.now(), LocalDateTime.now());
        accountRepository.save(account);

        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT, YearMonth.now().plusYears(1), new CardBilling(15, 5), null);
        Card card = Card.create(CARD_NUM, userId, new BankNumber("007"), details, LocalDateTime.now(), LocalDateTime.now());
        cardRepository.save(card);
    }

    @Test
    void upsertAccountFees_returns200AndSavedSchedule() throws Exception {
        AccountFeeScheduleRequest req = new AccountFeeScheduleRequest(
                new BigDecimal("4500.00"), null, "ARS", IvaTreatment.SEPARATE);

        mockMvc.perform(put("/api/v1/banks/accounts/" + CBU.value() + "/fees")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cbu").value(CBU.value()))
                .andExpect(jsonPath("$.data.maintenanceFee").value("4500.00"))
                .andExpect(jsonPath("$.data.ivaTreatment").value("SEPARATE"));
    }

    @Test
    void upsertCardFees_returns200AndSavedSchedule() throws Exception {
        CardFeeScheduleRequest req = new CardFeeScheduleRequest(
                new BigDecimal("80000.00"), new BigDecimal("3.50"), "ARS", IvaTreatment.INCLUDED);

        mockMvc.perform(put("/api/v1/banks/cards/" + CARD_NUM + "/fees")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cardNumber").value(CARD_NUM))
                .andExpect(jsonPath("$.data.annualFee").value("80000.00"))
                .andExpect(jsonPath("$.data.internationalSurchargePct").value("3.50"))
                .andExpect(jsonPath("$.data.ivaTreatment").value("INCLUDED"));
    }

    @Test
    void getUserFees_returnsAccountsWithTaxRateAndCards() throws Exception {
        mockMvc.perform(get("/api/v1/banks/fees")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accounts[0].cbu").value(CBU.value()))
                .andExpect(jsonPath("$.data.accounts[0].debitCreditTaxRate").value("0.006"))
                .andExpect(jsonPath("$.data.cards[0].cardNumber").value(CARD_NUM));
    }
}
