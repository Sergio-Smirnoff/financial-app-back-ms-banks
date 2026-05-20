package com.financialapp.banks.kafka.listener;

import com.financialapp.banks.kafka.event.TransactionCreatedEvent;
import com.financialapp.banks.model.entity.ProcessedEvent;
import com.financialapp.banks.repository.ProcessedEventRepository;
import com.financialapp.banks.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final AccountService accountService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "transaction.created", groupId = "banks-group")
    @Transactional
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Received transaction.created event: id={}, accountId={}, amount={}", 
                event.transactionId(), event.accountId(), event.amount());

        if (processedEventRepository.existsById(event.transactionId())) {
            log.warn("Event already processed: {}. Skipping.", event.transactionId());
            return;
        }

        try {
            // Basic security check: verify account ownership
            accountService.get(event.accountId(), event.userId());
            
            accountService.adjustBalance(event.accountId(), event.amount(), event.currency());
            
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.transactionId())
                    .build());
            
            log.info("Successfully adjusted balance for transaction: {}", event.transactionId());
        } catch (Exception e) {
            log.error("Failed to process transaction event {}: {}", event.transactionId(), e.getMessage());
            throw e;
        }
    }
}
