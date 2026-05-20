package com.financialapp.banks.infrastructure.scheduler;

import com.financialapp.banks.domain.model.Card;
import com.financialapp.banks.domain.model.Account;
import com.financialapp.banks.domain.model.LoanInstallment;
import com.financialapp.banks.infrastructure.persistence.CardRepository;
import com.financialapp.banks.infrastructure.persistence.LoanInstallmentRepository;
import com.financialapp.banks.infrastructure.persistence.AccountRepository;
import com.financialapp.banks.application.event.BankAlertEvent;
import com.financialapp.banks.infrastructure.event.BanksEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankAlertScheduler {

    private final CardRepository cardRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final AccountRepository accountRepository;
    private final BanksEventProducer eventProducer;

    @Scheduled(cron = "0 0 8 * * *") // Every day at 8 AM
    public void runDailyAlerts() {
        log.info("Running daily bank alerts scheduler...");
        checkCardExpirations();
        checkUpcomingLoanPayments();
        checkLowBalances();
    }

    private void checkCardExpirations() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(30);
        List<Card> expiring = cardRepository.findExpiringBetween(today, limit);
        
        log.info("Found {} card(s) expiring within 30 days", expiring.size());
        for (Card card : expiring) {
            Long bankId = card.getBankId();

            eventProducer.sendBankAlert(BankAlertEvent.builder()
                    .userId(card.getUserId())
                    .type("CARD_EXPIRING")
                    .title("Card Expiring Soon")
                    .message(String.format("Your card ending in %s expires on %s.", 
                            card.getLast4Digits(), card.getExpiringDate()))
                    .metadata(String.format("{\"cardId\":%d,\"bankId\":%d}", card.getId(), bankId))
                    .build());
        }
    }

    private void checkUpcomingLoanPayments() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(3);
        List<LoanInstallment> upcoming = loanInstallmentRepository.findUpcomingUnpaid(today, limit);

        log.info("Found {} loan installment(s) due within 3 days", upcoming.size());
        for (LoanInstallment inst : upcoming) {
            Long bankId = inst.getLoan().getBankId();

            eventProducer.sendBankAlert(BankAlertEvent.builder()
                    .userId(inst.getLoan().getUserId())
                    .type("LOAN_REMINDER")
                    .title("Loan Payment Due")
                    .message(String.format("Installment #%d of your loan '%s' is due on %s.",
                            inst.getInstallmentNumber(), inst.getLoan().getName(), inst.getDueDate()))
                    .metadata(String.format("{\"loanId\":%d,\"installmentId\":%d,\"bankId\":%d}", 
                            inst.getLoan().getId(), inst.getId(), bankId))
                    .build());
        }
    }

    private void checkLowBalances() {
        BigDecimal threshold = new BigDecimal("500.00");
        List<Account> lowBalanceAccounts = accountRepository.findLowBalanceAccounts(threshold);

        log.info("Found {} account(s) with balance below {}", lowBalanceAccounts.size(), threshold);
        for (Account account : lowBalanceAccounts) {
            eventProducer.sendBankAlert(BankAlertEvent.builder()
                    .userId(account.getUserId())
                    .type("LOW_BALANCE")
                    .title("Low Account Balance")
                    .message(String.format("Your account '%s' has a low balance of %s %s.",
                            account.getName(), account.getBalance(), account.getCurrency()))
                    .metadata(String.format("{\"accountId\":%d,\"bankId\":%d}", account.getId(), account.getBankId()))
                    .build());
        }
    }
}
