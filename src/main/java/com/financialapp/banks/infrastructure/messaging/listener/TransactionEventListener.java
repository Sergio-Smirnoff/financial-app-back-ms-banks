package com.financialapp.banks.infrastructure.messaging.listener;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionCreatedData;
import com.financialapp.commons.messaging.infrastructure.messaging.consume.IdempotentEventProcessor;
import io.cloudevents.CloudEvent;
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
    private final IdempotentEventProcessor processor;

    @KafkaListener(topics = "finances.transaction.created", groupId = "banks-group")
    @Transactional
    public void handleTransactionCreated(CloudEvent event) {
        processor.process(event, TransactionCreatedData.class, this::handle);
    }

    private void handle(TransactionCreatedData data) {
        log.info("Processing finances.transaction.created: id={}, accountCbu={}, amount={}",
                data.transactionId(), data.accountCbu(), data.amount());
        Currency currency = data.currency() != null ? Currency.getInstance(data.currency()) : null;
        adjustBalanceUseCase.execute(new AdjustBalanceCommand(
                data.accountCbu(),
                new Money(data.amount(), currency)
        ));
        log.info("Successfully adjusted balance for transaction: {}", data.transactionId());
    }
}
