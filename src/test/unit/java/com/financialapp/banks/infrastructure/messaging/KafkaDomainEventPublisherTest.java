package com.financialapp.banks.infrastructure.messaging;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.event.CardInstallmentPaidEvent;
import com.financialapp.banks.domain.event.LoanCreatedEvent;
import com.financialapp.banks.domain.event.LoanInstallmentPaidEvent;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionalKafkaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaDomainEventPublisherTest {

    @Mock ApplicationEventPublisher springPublisher;
    KafkaDomainEventPublisher publisher;

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final UserId USER = new UserId(1L);
    private static final BankNumber BANK = new BankNumber("007");

    @BeforeEach
    void setUp() {
        publisher = new KafkaDomainEventPublisher(springPublisher);
    }

    private TransactionalKafkaEvent capture() {
        ArgumentCaptor<TransactionalKafkaEvent> captor = ArgumentCaptor.forClass(TransactionalKafkaEvent.class);
        verify(springPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    private Money ars(String amount) {
        return new Money(new BigDecimal(amount), ARS);
    }

    @Test
    void loanCreated_queuesPaymentEvent() {
        // Given a loan-created event / When published
        publisher.publish(new LoanCreatedEvent(USER, "cbu", ars("1000.00"), "Car loan", LocalDate.of(2026, 6, 1)));

        // Then a payment-events message keyed by user id is queued
        TransactionalKafkaEvent sent = capture();
        assertThat(sent.topic()).isEqualTo("payment-events");
        assertThat(sent.key()).isEqualTo("1");
    }

    @Test
    void loanInstallmentPaid_queuesPaymentEvent() {
        // Given / When
        publisher.publish(new LoanInstallmentPaidEvent(USER, "cbu", ars("-100.00"), "Car loan", 2, LocalDate.of(2026, 6, 1)));

        // Then
        assertThat(capture().topic()).isEqualTo("payment-events");
    }

    @Test
    void cardInstallmentPaid_queuesPaymentEvent() {
        // Given / When
        publisher.publish(new CardInstallmentPaidEvent(USER, "cbu", ars("-50.00"), "Mac", 1, 3, LocalDate.of(2026, 6, 1)));

        // Then
        assertThat(capture().topic()).isEqualTo("payment-events");
    }

    @Test
    void lowBalance_queuesBankAlert() {
        // Given / When
        publisher.publish(new LowBalanceEvent(USER, "cbu", BANK, "Main", ars("100.00")));

        // Then
        assertThat(capture().topic()).isEqualTo("bank-alerts");
    }

    @Test
    void balanceAdjustedCredit_queuesBankAlert() {
        // Given a positive delta (credit branch) / When published
        publisher.publish(new BalanceAdjustedEvent(USER, "cbu", BANK, "Main", ars("250.00")));

        // Then a bank-alert is queued
        assertThat(capture().topic()).isEqualTo("bank-alerts");
    }

    @Test
    void balanceAdjustedDebit_queuesBankAlert() {
        // Given a negative delta (debit branch) / When published
        publisher.publish(new BalanceAdjustedEvent(USER, "cbu", BANK, "Main", ars("-250.00")));

        // Then a bank-alert is queued
        assertThat(capture().topic()).isEqualTo("bank-alerts");
    }

    @Test
    void unhandledEvent_publishesNothing() {
        // Given an event type the publisher does not handle
        DomainEvent unknown = new DomainEvent() {};

        // When published / Then nothing is queued (default branch)
        publisher.publish(unknown);

        verify(springPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }
}
