package com.financialapp.banks.infrastructure.messaging.listener;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionCreatedEvent;
import com.financialapp.banks.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final AdjustBalanceUseCase adjustBalanceUseCase;
    private final ProcessedEventJpaRepository processedEventRepository;

    @KafkaListener(topics = "transaction.created", groupId = "banks-group")
    @Transactional
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Received transaction.created event: id={}, accountCbu={}, amount={}",
                event.transactionId(), event.accountCbu(), event.amount());

        if (processedEventRepository.existsById(event.transactionId())) {
            log.warn("Event already processed: {}. Skipping.", event.transactionId());
            return;
        }

        try {
            Currency currency = event.currency() != null ? Currency.getInstance(event.currency()) : null;
            adjustBalanceUseCase.execute(new AdjustBalanceCommand(
                    event.accountCbu(),
                    new Money(event.amount(), currency)
            ));

            processedEventRepository.save(ProcessedEventJpaEntity.builder()
                    .eventId(event.transactionId())
                    .build());

            log.info("Successfully adjusted balance for transaction: {}", event.transactionId());
        } catch (Exception e) {
            log.error("Failed to process transaction event {}: {}", event.transactionId(), e.getMessage());
            throw e;
        }
    }
}
