package com.financialapp.banks.infrastructure.messaging.listener;

import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionCreatedEvent;
import com.financialapp.banks.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.ProcessedEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionEventListenerTest {

    @Mock AdjustBalanceUseCase adjustBalanceUseCase;
    @Mock ProcessedEventJpaRepository processedEventRepository;
    TransactionEventListener listener;

    private static final String CBU = "0070001600000000123459";

    @BeforeEach
    void setUp() {
        listener = new TransactionEventListener(adjustBalanceUseCase, processedEventRepository);
    }

    private TransactionCreatedEvent event(String currency) {
        return new TransactionCreatedEvent(100L, 1L, CBU, new BigDecimal("50.00"), currency, LocalDate.of(2026, 5, 1));
    }

    @Test
    void skips_whenEventAlreadyProcessed() {
        // Given the event id was already processed
        when(processedEventRepository.existsById(100L)).thenReturn(true);

        // When handled
        listener.handleTransactionCreated(event("ARS"));

        // Then the balance is not adjusted and nothing new is recorded
        verify(adjustBalanceUseCase, never()).execute(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void adjustsBalanceAndMarksProcessed_whenNew() {
        // Given a fresh event with a currency
        when(processedEventRepository.existsById(100L)).thenReturn(false);

        // When handled
        listener.handleTransactionCreated(event("ARS"));

        // Then the balance is adjusted and the event id is recorded as processed
        ArgumentCaptor<AdjustBalanceCommand> cmd = ArgumentCaptor.forClass(AdjustBalanceCommand.class);
        verify(adjustBalanceUseCase).execute(cmd.capture());
        assertThat(cmd.getValue().accountCbu()).isEqualTo(CBU);
        assertThat(cmd.getValue().delta().currency().getCurrencyCode()).isEqualTo("ARS");
        verify(processedEventRepository).save(any(ProcessedEventJpaEntity.class));
    }

    @Test
    void usesNullCurrency_whenEventCurrencyNull() {
        // Given a fresh event with no currency (the ternary null branch)
        when(processedEventRepository.existsById(100L)).thenReturn(false);

        // When handled
        listener.handleTransactionCreated(event(null));

        // Then the adjust command carries a currency-less delta
        ArgumentCaptor<AdjustBalanceCommand> cmd = ArgumentCaptor.forClass(AdjustBalanceCommand.class);
        verify(adjustBalanceUseCase).execute(cmd.capture());
        assertThat(cmd.getValue().delta().currency()).isNull();
    }

    @Test
    void rethrows_whenAdjustFails() {
        // Given the adjust use case fails
        when(processedEventRepository.existsById(100L)).thenReturn(false);
        doThrow(new RuntimeException("boom")).when(adjustBalanceUseCase).execute(any());

        // When/Then the failure propagates and the event is not marked processed
        assertThatThrownBy(() -> listener.handleTransactionCreated(event("ARS")))
                .isInstanceOf(RuntimeException.class);
        verify(processedEventRepository, never()).save(any());
    }
}
