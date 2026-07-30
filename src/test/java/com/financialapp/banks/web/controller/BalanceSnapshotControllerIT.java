package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.domain.repository.BalanceSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BalanceSnapshotControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private BalanceSnapshotRepository snapshotRepository;

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void getSnapshots_returnsSnapshotHistory() throws Exception {
        UserId userId = new UserId(1L);
        LocalDate today = LocalDate.now();
        BalanceSnapshot snapshot = BalanceSnapshot.create(
                userId, today,
                List.of(new Money(new BigDecimal("1000.00"), ARS)),
                List.of(),
                List.of()
        );
        snapshotRepository.save(snapshot);

        mockMvc.perform(get("/api/v1/banks/balance-snapshots")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .param("from", today.minusDays(1).toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].snapshotDate").value(today.toString()))
                .andExpect(jsonPath("$.data[0].cashByCurrency.ARS").value("1000.00"));
    }

    @Test
    void getSnapshots_invalidDateRange_returns400() throws Exception {
        LocalDate today = LocalDate.now();

        mockMvc.perform(get("/api/v1/banks/balance-snapshots")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .param("from", today.toString())
                        .param("to", today.minusDays(1).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_date_range"));
    }
}
