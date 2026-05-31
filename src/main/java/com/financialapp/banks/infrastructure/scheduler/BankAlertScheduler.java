package com.financialapp.banks.infrastructure.scheduler;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.infrastructure.messaging.payload.BankAlertEvent;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionalKafkaEvent;
import com.financialapp.banks.infrastructure.persistence.jpa.CardInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.LoanInstallmentJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Infrastructure scheduler issuing daily, all-user alerts. Because these are cross-aggregate,
 * all-user reads (not aggregate mutations), it queries the JPA installment repositories
 * directly as a read source rather than going through the aggregate roots.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BankAlertScheduler {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500.00");
    private static final int CARD_EXPIRY_WINDOW_DAYS = 30;
    private static final int LOAN_REMINDER_WINDOW_DAYS = 3;
    private static final int CARD_INSTALLMENT_WINDOW_DAYS = 3;

    private final CardRepository cardRepository;
    private final LoanInstallmentJpaRepository loanInstallmentJpaRepository;
    private final CardInstallmentJpaRepository cardInstallmentJpaRepository;
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher springPublisher;

    @Scheduled(cron = "0 0 8 * * *")
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
            String last4 = card.cardNumber().last4();
            sendAlert(card.userId().value(), BankAlertEvent.builder()
                    .userId(card.userId().value())
                    .type("CARD_EXPIRING")
                    .title("Card Expiring Soon")
                    .message(String.format("Your card ending in %s expires on %s.",
                            last4, card.details().expiringDate()))
                    .metadata(String.format("{\"cardNumber\":\"%s\",\"bankNumber\":\"%s\"}",
                            card.cardNumber().value(), card.bankNumber().value()))
                    .build());
        }
    }

    private void checkUpcomingLoanPayments() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(LOAN_REMINDER_WINDOW_DAYS);
        var upcoming = loanInstallmentJpaRepository.findUpcomingUnpaid(today, limit);

        log.info("Found {} loan installment(s) due within {} days", upcoming.size(), LOAN_REMINDER_WINDOW_DAYS);
        for (var inst : upcoming) {
            var loan = inst.getLoan();
            sendAlert(loan.getUserId(), BankAlertEvent.builder()
                    .userId(loan.getUserId())
                    .type("LOAN_REMINDER")
                    .title("Loan Payment Due")
                    .message(String.format("Installment #%d for loan '%s' is due on %s.",
                            inst.getInstallmentNumber(), loan.getName(), inst.getDueDate()))
                    .metadata(String.format("{\"loanId\":%d,\"installmentId\":%d}",
                            loan.getId(), inst.getId()))
                    .build());
        }
    }

    private void checkUpcomingCardInstallments() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(CARD_INSTALLMENT_WINDOW_DAYS);
        var upcoming = cardInstallmentJpaRepository.findAllUpcomingUnpaid(today, limit);

        log.info("Found {} card installment(s) due within {} days", upcoming.size(), CARD_INSTALLMENT_WINDOW_DAYS);
        for (var inst : upcoming) {
            var card = inst.getCard();
            sendAlert(card.getUserId(), BankAlertEvent.builder()
                    .userId(card.getUserId())
                    .type("PAYMENT_DUE")
                    .title("Card Installment Due")
                    .message(String.format("Installment %d/%d for '%s' is due on %s (%.2f %s).",
                            inst.getInstallmentNumber(), inst.getTotalInstallments(),
                            inst.getDescription(), inst.getDueDate(),
                            inst.getAmount(), inst.getCurrency()))
                    .metadata(String.format("{\"cardNumber\":\"%s\",\"installmentId\":%d}",
                            card.getCardNumber(), inst.getId()))
                    .build());
        }
    }

    private void checkLowBalances() {
        List<Account> lowBalance = accountRepository.findLowBalance(LOW_BALANCE_THRESHOLD);

        log.info("Found {} account(s) with balance below {}", lowBalance.size(), LOW_BALANCE_THRESHOLD);
        for (Account account : lowBalance) {
            sendAlert(account.userId().value(), BankAlertEvent.builder()
                    .userId(account.userId().value())
                    .type("LOW_BALANCE")
                    .title("Low Account Balance")
                    .message(String.format("Your account '%s' has a low balance of %s %s.",
                            account.name(),
                            account.balance().amount(),
                            account.balance().currency().getCurrencyCode()))
                    .metadata(String.format("{\"accountCbu\":\"%s\",\"bankNumber\":\"%s\"}",
                            account.cbu(), account.bankNumber().value()))
                    .build());
        }
    }

    private void sendAlert(Long userId, BankAlertEvent payload) {
        String key = userId != null ? userId.toString() : "scheduler";
        springPublisher.publishEvent(new TransactionalKafkaEvent("bank-alerts", key, payload));
    }
}
