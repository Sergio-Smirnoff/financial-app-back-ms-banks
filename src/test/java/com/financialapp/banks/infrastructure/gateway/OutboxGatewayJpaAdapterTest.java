package com.financialapp.banks.infrastructure.gateway;

import com.financialapp.banks.infrastructure.persistence.entity.OutboxEventEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import com.financialapp.commons.messaging.domain.model.EventId;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxGatewayJpaAdapterTest {

    @Mock
    OutboxEventJpaRepository repository;

    @InjectMocks
    OutboxGatewayJpaAdapter adapter;

    @Test
    void save_persistsAllFieldsFromRecord() {
        OutboxRecord record = OutboxRecord.create(
                "banks.payment.recorded", "42",
                new EventType("banks.payment.recorded"),
                "ms-banks",
                "https://schemas.financial-app/banks/payment-recorded/v1",
                "{\"userId\":42}");

        adapter.save(record);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(repository).save(captor.capture());
        OutboxEventEntity saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(record.eventId().value());
        assertThat(saved.getTopic()).isEqualTo("banks.payment.recorded");
        assertThat(saved.getAggregateKey()).isEqualTo("42");
        assertThat(saved.getCeType()).isEqualTo("banks.payment.recorded");
        assertThat(saved.getCeSource()).isEqualTo("ms-banks");
        assertThat(saved.isSent()).isFalse();
    }

    @Test
    void markSent_setSentFlagAndSentAt() {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId("some-uuid");
        entity.setSent(false);
        when(repository.findByEventId("some-uuid")).thenReturn(Optional.of(entity));

        adapter.markSent(new EventId("some-uuid"));

        verify(repository).save(entity);
        assertThat(entity.isSent()).isTrue();
        assertThat(entity.getSentAt()).isNotNull();
    }
}
