package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Covers CardController endpoints not exercised by CardControllerIT: get, filtered list, update, delete. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CardControllerExtraIT {

    @Autowired MockMvc mockMvc;
    @Autowired CardRepository cardRepository;

    private static final String PAN = "4111111111111111";

    @BeforeEach
    void seedCard() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.now().plusYears(2), new CardBilling(20, 10));
        cardRepository.save(new CreditCard(CardNumber.from(PAN), new UserId(1L), new BankNumber("007"),
                details, LocalDateTime.now(), LocalDateTime.now(), List.of()));
    }

    @Test
    void getCard_returnsSeededCard() throws Exception {
        mockMvc.perform(get("/api/v1/banks/cards/{c}", PAN)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cardNumber").value(PAN));
    }

    @Test
    void listCards_filteredByBankNumber() throws Exception {
        mockMvc.perform(get("/api/v1/banks/cards").param("bankNumber", "007")
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].cardNumber").value(PAN));
    }

    @Test
    void updateCard_changesBillingAndExpiry() throws Exception {
        String body = "{\"expiringDate\":\"08/30\",\"closingDay\":15,\"dueDay\":5}";
        mockMvc.perform(patch("/api/v1/banks/cards/{c}", PAN)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.closingDay").value(15));
    }

    @Test
    void deleteCard_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/banks/cards/{c}", PAN)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk());
    }
}
