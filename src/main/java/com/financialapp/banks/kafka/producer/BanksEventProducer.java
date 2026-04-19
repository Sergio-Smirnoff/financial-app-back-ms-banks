package com.financialapp.banks.kafka.producer;

import com.financialapp.banks.kafka.event.BankAlertEvent;
import com.financialapp.banks.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BanksEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentEvent(PaymentEvent event) {
        log.info("Sending payment event: {}", event);
        kafkaTemplate.send("payment-events", event.userId().toString(), event);
    }

    public void sendBankAlert(BankAlertEvent event) {
        log.info("Sending bank alert event: {}", event);
        kafkaTemplate.send("bank-alerts", event.getUserId().toString(), event);
    }
}
