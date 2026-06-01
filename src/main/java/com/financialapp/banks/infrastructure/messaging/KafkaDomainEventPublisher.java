package com.financialapp.banks.infrastructure.messaging;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.event.*;
import com.financialapp.banks.domain.port.DomainEventPublisher;
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
                    PaymentEvent.builder()
                            .userId(e.userId().value())
                            .accountCbu(e.destinationAccountCbu())
                            .amount(e.amount().amount())
                            .currency(e.amount().currency().getCurrencyCode())
                            .description("Loan Deposit: " + e.loanName())
                            .date(e.date())
                            .build()
            );
            case LoanInstallmentPaidEvent e -> sendPayment(
                    e.userId().value(),
                    PaymentEvent.builder()
                            .userId(e.userId().value())
                            .accountCbu(e.accountCbu())
                            .amount(e.amount().amount())
                            .currency(e.amount().currency().getCurrencyCode())
                            .description("Loan Payment: " + e.loanName() + " (Installment " + e.installmentNumber() + ")")
                            .date(e.paidDate())
                            .build()
            );
            case CardInstallmentPaidEvent e -> sendPayment(
                    e.userId().value(),
                    PaymentEvent.builder()
                            .userId(e.userId().value())
                            .accountCbu(e.accountCbu())
                            .amount(e.amount().amount())
                            .currency(e.amount().currency().getCurrencyCode())
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
                                    e.accountName(), e.balance().amount(), e.balance().currency().getCurrencyCode()))
                            .metadata(String.format("{\"accountCbu\":\"%s\",\"bankNumber\":\"%s\",\"balance\":%.2f}",
                                    e.accountCbu(), e.bankNumber().value(), e.balance().amount()))
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
                                .metadata(String.format("{\"accountCbu\":\"%s\",\"bankNumber\":\"%s\",\"amount\":%.2f}",
                                        e.accountCbu(), e.bankNumber().value(), e.delta().amount().abs()))
                                .build()
                );
            }
            default -> log.warn("Unhandled domain event: {}", event.getClass().getSimpleName());
        }
    }

    private void sendPayment(Long userId, PaymentEvent payload) {
        log.info("Queuing payment event for user {}", userId);
        springPublisher.publishEvent(new TransactionalKafkaEvent("payment-events", userId.toString(), payload));
    }

    private void sendAlert(Long userId, BankAlertEvent payload) {
        log.info("Queuing bank alert for user {}", userId);
        springPublisher.publishEvent(new TransactionalKafkaEvent("bank-alerts", userId.toString(), payload));
    }
}
