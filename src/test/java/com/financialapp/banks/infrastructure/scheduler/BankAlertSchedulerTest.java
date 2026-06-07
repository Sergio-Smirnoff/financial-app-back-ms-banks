package com.financialapp.banks.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountNumber;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.bank.SucursalCode;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.infrastructure.persistence.entity.LoanInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.CardInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.LoanInstallmentJpaRepository;
import com.financialapp.commons.messaging.domain.gateway.OutboxGateway;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAlertSchedulerTest {

    @Mock
    CardRepository cardRepository;
    @Mock
    LoanInstallmentJpaRepository loanInstallmentJpaRepository;
    @Mock
    CardInstallmentJpaRepository cardInstallmentJpaRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    OutboxGateway outboxGateway;

    BankAlertScheduler scheduler;

    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private void init() {
        scheduler = new BankAlertScheduler(cardRepository, loanInstallmentJpaRepository,
                cardInstallmentJpaRepository, accountRepository, outboxGateway, objectMapper());
    }

    private Account lowBalanceAccount() {
        return new SavingsAccount(
                new Cbu(new BankNumber("072"), new SucursalCode("0001"), new AccountNumber("0000000001234")),
                "alias",
                new Money(new BigDecimal("100.00"), Currency.getInstance("ARS")),
                new UserId(11L),
                new BankNumber("072"),
                "Savings ARS",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void checkLowBalances_publishesLowBalanceOutboxRecord() {
        init();
        when(cardRepository.findExpiringBetween(any(), any())).thenReturn(List.of());
        when(loanInstallmentJpaRepository.findUpcomingUnpaid(any(), any())).thenReturn(List.of());
        when(cardInstallmentJpaRepository.findAllUpcomingUnpaid(any(), any())).thenReturn(List.of());
        when(accountRepository.findLowBalance(any())).thenReturn(List.of(lowBalanceAccount()));

        scheduler.runDailyAlerts();

        ArgumentCaptor<OutboxRecord> captor = ArgumentCaptor.forClass(OutboxRecord.class);
        verify(outboxGateway, atLeastOnce()).save(captor.capture());
        OutboxRecord record = captor.getAllValues().stream()
                .filter(r -> "banks.account.low_balance".equals(r.topic()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No low_balance record published"));
        assertThat(record.key()).isEqualTo("11");
        assertThat(record.dataJson()).contains("Savings ARS");
        assertThat(record.dataJson()).contains("072");
        assertThat(record.dataJson()).contains("100.00");
    }

    @Test
    void checkUpcomingLoanPayments_publishesLoanReminderOutboxRecord() {
        init();
        LoanJpaEntity loan = new LoanJpaEntity();
        loan.setId(55L);
        loan.setUserId(22L);
        loan.setName("Home Loan");
        loan.setCurrency("ARS");

        LoanInstallmentJpaEntity inst = new LoanInstallmentJpaEntity();
        inst.setId(99L);
        inst.setLoan(loan);
        inst.setInstallmentNumber(3);
        inst.setDueDate(LocalDate.now().plusDays(2));
        inst.setAmount(new BigDecimal("1500.00"));

        when(cardRepository.findExpiringBetween(any(), any())).thenReturn(List.of());
        when(loanInstallmentJpaRepository.findUpcomingUnpaid(any(), any())).thenReturn(List.of(inst));
        when(cardInstallmentJpaRepository.findAllUpcomingUnpaid(any(), any())).thenReturn(List.of());
        when(accountRepository.findLowBalance(any())).thenReturn(List.of());

        scheduler.runDailyAlerts();

        ArgumentCaptor<OutboxRecord> captor = ArgumentCaptor.forClass(OutboxRecord.class);
        verify(outboxGateway, atLeastOnce()).save(captor.capture());
        OutboxRecord record = captor.getAllValues().stream()
                .filter(r -> "banks.loan.reminder".equals(r.topic()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No loan.reminder record published"));
        assertThat(record.key()).isEqualTo("22");
        assertThat(record.dataJson()).contains("Home Loan");
        assertThat(record.dataJson()).contains("55");
        assertThat(record.dataJson()).contains("99");
    }
}
