package com.financialapp.banks.infrastructure.messaging.listener;

import com.financialapp.banks.infrastructure.messaging.payload.TransactionalKafkaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionalKafkaListenerTest {

    @Mock KafkaTemplate<String, Object> kafkaTemplate;
    TransactionalKafkaListener listener;

    @BeforeEach
    void setUp() {
        listener = new TransactionalKafkaListener(kafkaTemplate);
    }

    @Test
    void handle_forwardsEventToKafkaTemplate() {
        // Given a transactional kafka event
        Object payload = new Object();
        TransactionalKafkaEvent event = new TransactionalKafkaEvent("bank-alerts", "1", payload);

        // When the after-commit handler runs
        listener.handle(event);

        // Then it is sent to the event's topic with its key and payload
        verify(kafkaTemplate).send("bank-alerts", "1", payload);
    }
}
