package com.financialapp.banks.infrastructure.scheduler;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionalKafkaEvent;
import com.financialapp.banks.infrastructure.persistence.entity.CardInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.CardInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.LoanInstallmentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAlertSchedulerTest {

    @Mock CardRepository cardRepository;
    @Mock LoanInstallmentJpaRepository loanInstallmentJpaRepository;
    @Mock CardInstallmentJpaRepository cardInstallmentJpaRepository;
    @Mock AccountRepository accountRepository;
    @Mock ApplicationEventPublisher springPublisher;
    BankAlertScheduler scheduler;

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);

    @BeforeEach
    void setUp() {
        scheduler = new BankAlertScheduler(cardRepository, loanInstallmentJpaRepository,
                cardInstallmentJpaRepository, accountRepository, springPublisher);
    }

    private Card expiringCard() {
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.now().plusMonths(1), new CardBilling(20, 10));
        return Card.create("4111111111111111", new UserId(1L), new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private LoanInstallmentJpaEntity dueLoanInstallment() {
        LoanJpaEntity loan = LoanJpaEntity.builder().id(5L).userId(2L).name("Car loan").build();
        return LoanInstallmentJpaEntity.builder().id(10L).loan(loan).installmentNumber(1).dueDate(TODAY).build();
    }

    private CardInstallmentJpaEntity dueCardInstallmentWithNullUser() {
        // Card owner userId is null -> exercises the "scheduler" fallback key in sendAlert
        CardJpaEntity card = CardJpaEntity.builder().cardNumber("4111111111111111").userId(null).build();
        return CardInstallmentJpaEntity.builder().id(20L).card(card).description("Mac")
                .installmentNumber(1).totalInstallments(3).amount(new BigDecimal("100.00")).currency("USD")
                .dueDate(TODAY).build();
    }

    private Account lowBalanceAccount() {
        return new CheckingAccount(Cbu.from("0070001600000000123459"), "alias",
                new Money(new BigDecimal("100.00"), ARS), new UserId(3L), new BankNumber("007"),
                "Main", true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void runDailyAlerts_publishesAnAlertForEachCheck() {
        // Given one item surfaced by each of the four checks
        when(cardRepository.findExpiringBetween(any(), any())).thenReturn(List.of(expiringCard()));
        when(loanInstallmentJpaRepository.findUpcomingUnpaid(any(), any())).thenReturn(List.of(dueLoanInstallment()));
        when(cardInstallmentJpaRepository.findAllUpcomingUnpaid(any(), any()))
                .thenReturn(List.of(dueCardInstallmentWithNullUser()));
        when(accountRepository.findLowBalance(any())).thenReturn(List.of(lowBalanceAccount()));

        // When the daily scheduler runs
        scheduler.runDailyAlerts();

        // Then one bank-alert is queued per check (card expiry, loan reminder, card installment, low balance)
        verify(springPublisher, times(4)).publishEvent(any(TransactionalKafkaEvent.class));
    }
}
