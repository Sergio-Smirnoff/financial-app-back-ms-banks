package com.financialapp.banks.infrastructure.scheduler;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.repository.LoanInstallmentRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.infrastructure.messaging.payload.BankAlertEvent;
import com.financialapp.banks.infrastructure.messaging.payload.TransactionalKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    private final CardRepository cardRepository;
    private final CardInstallmentRepository cardInstallmentRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final LoanRepository loanRepository;
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
                    .metadata(String.format("{\"cardNumber\":\"%s\",\"bankName\":\"%s\"}",
                            card.cardNumber().value(), card.bankName()))
                    .build());
        }
    }

    private void checkUpcomingLoanPayments() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(LOAN_REMINDER_WINDOW_DAYS);
        List<LoanInstallment> upcoming = loanInstallmentRepository.findUpcomingUnpaid(today, limit);

        log.info("Found {} loan installment(s) due within {} days", upcoming.size(), LOAN_REMINDER_WINDOW_DAYS);
        for (LoanInstallment inst : upcoming) {
            loanRepository.findById(inst.loanId()).ifPresent(loan ->
                sendAlert(loan.userId().value(), BankAlertEvent.builder()
                        .userId(loan.userId().value())
                        .type("LOAN_REMINDER")
                        .title("Loan Payment Due")
                        .message(String.format("Installment #%d for loan '%s' is due on %s.",
                                inst.installmentNumber(), loan.name(), inst.dueDate()))
                        .metadata(String.format("{\"loanId\":%d,\"installmentId\":%d}",
                                inst.loanId().value(), inst.id().value()))
                        .build())
            );
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
                    .metadata(String.format("{\"accountCbu\":\"%s\",\"bankName\":\"%s\"}",
                            account.cbu(), account.bankName()))
                    .build());
        }
    }

    private void checkUpcomingCardInstallments() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(CARD_INSTALLMENT_WINDOW_DAYS);
        List<CardInstallment> upcoming = cardInstallmentRepository.findUpcomingUnpaid(today, limit);

        log.info("Found {} card installment(s) due within {} days", upcoming.size(), CARD_INSTALLMENT_WINDOW_DAYS);
        for (CardInstallment inst : upcoming) {
            cardRepository.findByCardNumber(inst.cardNumber()).ifPresent(card ->
                sendAlert(card.userId().value(), BankAlertEvent.builder()
                        .userId(card.userId().value())
                        .type("PAYMENT_DUE")
                        .title("Card Installment Due")
                        .message(String.format("Installment %d/%d for '%s' is due on %s (%.2f %s).",
                                inst.installmentNumber(), inst.totalInstallments(),
                                inst.description(), inst.dueDate(),
                                inst.amount().amount(), inst.amount().currency().getCurrencyCode()))
                        .metadata(String.format("{\"cardNumber\":\"%s\",\"installmentId\":%d}",
                                inst.cardNumber(), inst.id().value()))
                        .build())
            );
        }
    }

    private void sendAlert(Long userId, BankAlertEvent payload) {
        String key = userId != null ? userId.toString() : "scheduler";
        springPublisher.publishEvent(new TransactionalKafkaEvent("bank-alerts", key, payload));
    }
}
