package com.financialapp.banks.kafka.producer;

import com.financialapp.banks.kafka.event.BankAlertEvent;
import com.financialapp.banks.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BanksEventProducer {

    private final ApplicationEventPublisher eventPublisher;

    public void sendPaymentEvent(PaymentEvent event) {
        log.info("Queuing transactional payment event: {}", event);
        eventPublisher.publishEvent(new TransactionalKafkaEvent("payment-events", event.userId().toString(), event));
    }

    public void sendBankAlert(BankAlertEvent event) {
        log.info("Queuing transactional bank alert event: {}", event);
        eventPublisher.publishEvent(new TransactionalKafkaEvent("bank-alerts", event.getUserId().toString(), event));
    }
}
