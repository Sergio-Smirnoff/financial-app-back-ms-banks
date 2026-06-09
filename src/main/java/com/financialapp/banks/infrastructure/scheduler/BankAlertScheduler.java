package com.financialapp.banks.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.infrastructure.messaging.payload.CardExpiringData;
import com.financialapp.banks.infrastructure.messaging.payload.CardInstallmentDueData;
import com.financialapp.banks.infrastructure.messaging.payload.LoanReminderData;
import com.financialapp.banks.infrastructure.messaging.payload.LowBalanceData;
import com.financialapp.banks.infrastructure.persistence.jpa.CardInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.LoanInstallmentJpaRepository;
import com.financialapp.commons.messaging.domain.gateway.OutboxGateway;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankAlertScheduler {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500.00");
    private static final int CARD_EXPIRY_WINDOW_DAYS = 30;
    private static final int LOAN_REMINDER_WINDOW_DAYS = 3;
    private static final int CARD_INSTALLMENT_WINDOW_DAYS = 3;

    private static final String SOURCE = "ms-banks";

    private final CardRepository cardRepository;
    private final LoanInstallmentJpaRepository loanInstallmentJpaRepository;
    private final CardInstallmentJpaRepository cardInstallmentJpaRepository;
    private final AccountRepository accountRepository;
    private final OutboxGateway outboxGateway;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void runDailyAlerts() {
        log.info("Running daily bank alerts scheduler...");
        checkCardExpirations();
        checkUpcomingLoanPayments();
        checkUpcomingCardInstallments();
        checkLowBalances();
    }

    private void checkCardExpirations() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(CARD_EXPIRY_WINDOW_DAYS);
        List<Card> expiring = cardRepository.findExpiringBetween(today, limit);

        log.info("Found {} card(s) expiring within {} days", expiring.size(), CARD_EXPIRY_WINDOW_DAYS);
        for (Card card : expiring) {
            CardExpiringData data = new CardExpiringData(
                    card.userId().value(),
                    card.cardNumber().value(),
                    card.bankNumber().value(),
                    card.details().expiringDate().toString()
            );
            publish("banks.card.expiring",
                    "https://schemas.financial-app/banks/card-expiring/v1",
                    card.userId().value(), data);
        }
    }

    private void checkUpcomingLoanPayments() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(LOAN_REMINDER_WINDOW_DAYS);
        var upcoming = loanInstallmentJpaRepository.findUpcomingUnpaid(today, limit);

        log.info("Found {} loan installment(s) due within {} days", upcoming.size(), LOAN_REMINDER_WINDOW_DAYS);
        for (var inst : upcoming) {
            var loan = inst.getLoan();
            LoanReminderData data = new LoanReminderData(
                    loan.getUserId(),
                    loan.getId(),
                    inst.getId(),
                    inst.getInstallmentNumber(),
                    loan.getName(),
                    inst.getDueDate()
            );
            publish("banks.loan.reminder",
                    "https://schemas.financial-app/banks/loan-reminder/v1",
                    loan.getUserId(), data);
        }
    }

    private void checkUpcomingCardInstallments() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(CARD_INSTALLMENT_WINDOW_DAYS);
        var upcoming = cardInstallmentJpaRepository.findAllUpcomingUnpaid(today, limit);

        log.info("Found {} card installment(s) due within {} days", upcoming.size(), CARD_INSTALLMENT_WINDOW_DAYS);
        for (var inst : upcoming) {
            var card = inst.getCard();
            CardInstallmentDueData data = new CardInstallmentDueData(
                    card.getUserId(),
                    card.getCardNumber(),
                    inst.getId(),
                    inst.getInstallmentNumber(),
                    inst.getTotalInstallments(),
                    inst.getDescription(),
                    inst.getDueDate(),
                    inst.getAmount(),
                    inst.getCurrency()
            );
            publish("banks.card.installment_due",
                    "https://schemas.financial-app/banks/card-installment-due/v1",
                    card.getUserId(), data);
        }
    }

    private void checkLowBalances() {
        List<Account> lowBalance = accountRepository.findLowBalance(LOW_BALANCE_THRESHOLD);

        log.info("Found {} account(s) with balance below {}", lowBalance.size(), LOW_BALANCE_THRESHOLD);
        for (Account account : lowBalance) {
            LowBalanceData data = new LowBalanceData(
                    account.userId().value(),
                    account.name(),
                    account.cbu().toString(),
                    account.bankNumber().value(),
                    account.balance().amount(),
                    account.balance().currency().getCurrencyCode()
            );
            publish("banks.account.low_balance",
                    "https://schemas.financial-app/banks/account-low-balance/v1",
                    account.userId().value(), data);
        }
    }

    private void publish(String topic, String schema, Long userId, Object data) {
        String key = userId != null ? userId.toString() : "scheduler";
        try {
            String json = objectMapper.writeValueAsString(data);
            OutboxRecord record = OutboxRecord.create(topic, key, new EventType(topic), SOURCE, schema, json);
            outboxGateway.save(record);
        } catch (Exception ex) {
            log.error("Failed to publish scheduler event for topic={}", topic, ex);
            throw new IllegalStateException("Failed to publish scheduler event for topic=" + topic, ex);
        }
    }
}
