package com.financialapp.banks.infrastructure.scheduler;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.card.BillingPeriod;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BalanceSnapshotRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.domain.service.CardBillingCycle;
import com.financialapp.banks.domain.service.ComputeBalanceSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceSnapshotScheduler {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final LoanRepository loanRepository;
    private final BalanceSnapshotRepository snapshotRepository;
    private final ComputeBalanceSnapshot computeBalanceSnapshot = new ComputeBalanceSnapshot();
    private final CardBillingCycle cardBillingCycle = new CardBillingCycle();

    @Scheduled(cron = "0 0 0 * * *")
    public void captureDailySnapshots() {
        log.info("Starting daily balance snapshot capture");
        List<UserId> userIds = accountRepository.findDistinctOwners();
        LocalDate today = LocalDate.now();

        for (UserId userId : userIds) {
            try {
                List<Account> accounts = accountRepository.findByUserId(userId);
                List<Card> cards = cardRepository.findByUserId(userId);
                List<Loan> loans = loanRepository.findByUserId(userId);

                List<CardInstallment> currentPeriodInstallments = new ArrayList<>();
                for (Card card : cards) {
                    if (card instanceof CreditCard creditCard) {
                        BillingPeriod period = cardBillingCycle.currentPeriod(card.details().billing(), today);
                        for (CardInstallment installment : creditCard.installments()) {
                            if (!installment.dueDate().isAfter(period.dueDate())) {
                                currentPeriodInstallments.add(installment);
                            }
                        }
                    }
                }

                BalanceSnapshot snapshot = computeBalanceSnapshot.compute(
                        userId, today, accounts, cards, currentPeriodInstallments, loans);
                snapshotRepository.save(snapshot);
            } catch (Exception e) {
                log.error("Failed to capture balance snapshot for user {}", userId.value(), e);
            }
        }
        log.info("Daily balance snapshot capture finished");
    }
}
