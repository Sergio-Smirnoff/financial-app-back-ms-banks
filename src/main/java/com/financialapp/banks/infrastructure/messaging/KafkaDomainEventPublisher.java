package com.financialapp.banks.infrastructure.messaging;

import com.financialapp.banks.domain.event.*;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.shared.DomainEvent;
import com.financialapp.banks.infrastructure.messaging.payload.BankAlertEvent;
import com.financialapp.banks.infrastructure.messaging.payload.PaymentEvent;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionalKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;

    @Override
    public void publish(DomainEvent event) {
        switch (event) {
            case LoanCreatedEvent e -> sendPayment(
                    e.userId().value(),
                    e.destinationAccountId().value(),
                    PaymentEvent.builder()
                            .userId(e.userId().value())
                            .accountId(e.destinationAccountId().value())
                            .amount(e.amount())
                            .description("Loan Deposit: " + e.loanName())
                            .date(e.date())
                            .build()
            );
            case LoanInstallmentPaidEvent e -> sendPayment(
                    e.userId().value(),
                    e.accountId().value(),
                    PaymentEvent.builder()
                            .userId(e.userId().value())
                            .accountId(e.accountId().value())
                            .amount(e.amount())
                            .description("Loan Payment: " + e.loanName() + " (Installment " + e.installmentNumber() + ")")
                            .date(e.paidDate())
                            .build()
            );
            case CardInstallmentPaidEvent e -> sendPayment(
                    e.userId().value(),
                    e.accountId().value(),
                    PaymentEvent.builder()
                            .userId(e.userId().value())
                            .accountId(e.accountId().value())
                            .amount(e.amount())
                            .description("Card Installment: " + e.description() +
                                    " (" + e.installmentNumber() + "/" + e.totalInstallments() + ")")
                            .date(e.paidDate())
                            .build()
            );
            case LowBalanceEvent e -> sendAlert(
                    e.userId().value(),
                    BankAlertEvent.builder()
                            .userId(e.userId().value())
                            .type("LOW_BALANCE")
                            .title("Low Account Balance")
                            .message(String.format("Account '%s' has a low balance of %.2f %s.",
                                    e.accountName(), e.balance(), e.currency()))
                            .metadata(String.format("{\"accountId\":%d,\"bankName\":\"%s\",\"balance\":%.2f}",
                                    e.accountId().value(), e.bankName(), e.balance()))
                            .build()
            );
            case BalanceAdjustedEvent e -> {
                boolean credit = e.delta().amount().signum() >= 0;
                sendAlert(
                        e.userId().value(),
                        BankAlertEvent.builder()
                                .userId(e.userId().value())
                                .type(credit ? "TRANSFER_RECEIVED" : "TRANSFER_SENT")
                                .title(credit ? "Funds Received" : "Funds Sent")
                                .message(String.format("%.2f %s has been %s account '%s'.",
                                        e.delta().amount().abs(),
                                        e.delta().currency().getCurrencyCode(),
                                        credit ? "credited to" : "debited from",
                                        e.accountName()))
                                .metadata(String.format("{\"accountId\":%d,\"bankName\":\"%s\",\"amount\":%.2f}",
                                        e.accountId().value(), e.bankName(), e.delta().amount().abs()))
                                .build()
                );
            }
            default -> log.warn("Unhandled domain event: {}", event.getClass().getSimpleName());
        }
    }

    private void sendPayment(Long userId, Long accountId, PaymentEvent payload) {
        log.info("Queuing payment event for user {}", userId);
        springPublisher.publishEvent(new TransactionalKafkaEvent("payment-events", userId.toString(), payload));
    }

    private void sendAlert(Long userId, BankAlertEvent payload) {
        log.info("Queuing bank alert for user {}", userId);
        springPublisher.publishEvent(new TransactionalKafkaEvent("bank-alerts", userId.toString(), payload));
    }
}
