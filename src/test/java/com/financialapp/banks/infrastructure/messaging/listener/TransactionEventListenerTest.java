package com.financialapp.banks.infrastructure.messaging.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionCreatedData;
import com.financialapp.commons.messaging.domain.gateway.ProcessedEventGateway;
import com.financialapp.commons.messaging.domain.model.EventId;
import com.financialapp.commons.messaging.infrastructure.messaging.consume.IdempotentEventProcessor;
import com.financialapp.commons.messaging.infrastructure.messaging.serde.CloudEventSerde;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionEventListenerTest {

    @Mock
    AdjustBalanceUseCase adjustBalanceUseCase;

    @Mock
    ProcessedEventGateway processedEventGateway;

    TransactionEventListener listener;

    @BeforeEach
    void setUp() {
        IdempotentEventProcessor processor = new IdempotentEventProcessor(
                processedEventGateway, new CloudEventSerde(new ObjectMapper()));
        listener = new TransactionEventListener(adjustBalanceUseCase, processor);
    }

    @Test
    void handlesNewEvent_callsAdjustBalance() throws Exception {
        TransactionCreatedData data = new TransactionCreatedData(
                101L, "1234567890123456789012", new BigDecimal("250.00"), "ARS");
        String json = new ObjectMapper().writeValueAsString(data);

        CloudEvent event = CloudEventBuilder.v1()
                .withId("tx-ce-1")
                .withSource(URI.create("/financial-app/ms-finances"))
                .withType("finances.transaction.created")
                .withData("application/json", json.getBytes(StandardCharsets.UTF_8))
                .build();

        when(processedEventGateway.isProcessed(new EventId("tx-ce-1"))).thenReturn(false);

        listener.handleTransactionCreated(event);

        ArgumentCaptor<AdjustBalanceCommand> captor = ArgumentCaptor.forClass(AdjustBalanceCommand.class);
        verify(adjustBalanceUseCase).execute(captor.capture());
        AdjustBalanceCommand cmd = captor.getValue();
        assertThat(cmd.accountCbu()).isEqualTo("1234567890123456789012");
        assertThat(cmd.delta().amount()).isEqualByComparingTo(new BigDecimal("250.00"));
        verify(processedEventGateway).markProcessed(new EventId("tx-ce-1"));
    }

    @Test
    void skipsDuplicateEvent() throws Exception {
        TransactionCreatedData data = new TransactionCreatedData(
                102L, "1234567890123456789012", new BigDecimal("100.00"), "ARS");
        String json = new ObjectMapper().writeValueAsString(data);

        CloudEvent event = CloudEventBuilder.v1()
                .withId("tx-ce-dup")
                .withSource(URI.create("/financial-app/ms-finances"))
                .withType("finances.transaction.created")
                .withData("application/json", json.getBytes(StandardCharsets.UTF_8))
                .build();

        when(processedEventGateway.isProcessed(new EventId("tx-ce-dup"))).thenReturn(true);

        listener.handleTransactionCreated(event);

        verify(adjustBalanceUseCase, never()).execute(org.mockito.ArgumentMatchers.any());
    }
}
