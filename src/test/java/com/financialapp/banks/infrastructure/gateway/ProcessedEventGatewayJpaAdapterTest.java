package com.financialapp.banks.infrastructure.gateway;

import com.financialapp.banks.infrastructure.persistence.jpa.InboundEventJpaRepository;
import com.financialapp.commons.messaging.domain.model.EventId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedEventGatewayJpaAdapterTest {

    @Mock
    InboundEventJpaRepository repository;

    @InjectMocks
    ProcessedEventGatewayJpaAdapter adapter;

    @Test
    void isProcessed_returnsTrueWhenEventExists() {
        when(repository.existsById("ce-id-1")).thenReturn(true);
        assertThat(adapter.isProcessed(new EventId("ce-id-1"))).isTrue();
    }

    @Test
    void isProcessed_returnsFalseWhenEventAbsent() {
        when(repository.existsById("ce-id-2")).thenReturn(false);
        assertThat(adapter.isProcessed(new EventId("ce-id-2"))).isFalse();
    }

    @Test
    void markProcessed_savesEntityWithCorrectId() {
        adapter.markProcessed(new EventId("ce-id-3"));
        verify(repository).save(argThat(e ->
                "ce-id-3".equals(e.getEventId())));
    }
}
