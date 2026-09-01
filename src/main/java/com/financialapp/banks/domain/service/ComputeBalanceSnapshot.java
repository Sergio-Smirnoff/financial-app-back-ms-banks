package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeBalanceSnapshot {

    public BalanceSnapshot compute(UserId userId,
                                   LocalDate snapshotDate,
                                   List<Account> accounts,
                                   List<Card> cards,
                                   List<CardInstallment> currentPeriodInstallments,
                                   List<Loan> loans) {
        List<Money> cashByCurrency = computeCash(accounts);
        List<Money> cardDebtByCurrency = computeCardDebt(currentPeriodInstallments);
        List<Money> loanDebtByCurrency = computeLoanDebt(loans);

        return BalanceSnapshot.create(userId, snapshotDate, cashByCurrency, cardDebtByCurrency, loanDebtByCurrency);
    }

    private List<Money> computeCash(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return List.of();
        }
        Map<Currency, BigDecimal> totals = new HashMap<>();
        for (Account account : accounts) {
            if (account != null && Boolean.TRUE.equals(account.isActive()) && account.balance() != null) {
                Currency currency = account.balance().currency();
                totals.merge(currency, account.balance().amount(), BigDecimal::add);
            }
        }
        return totals.entrySet().stream()
                .map(entry -> new Money(entry.getValue(), entry.getKey()))
                .toList();
    }

    private List<Money> computeCardDebt(List<CardInstallment> installments) {
        if (installments == null || installments.isEmpty()) {
            return List.of();
        }
        Map<Currency, BigDecimal> totals = new HashMap<>();
        for (CardInstallment installment : installments) {
            if (installment != null && !installment.paid() && installment.amount() != null) {
                Currency currency = installment.amount().currency();
                totals.merge(currency, installment.amount().amount(), BigDecimal::add);
            }
        }
        return totals.entrySet().stream()
                .map(entry -> new Money(entry.getValue(), entry.getKey()))
                .toList();
    }

    private List<Money> computeLoanDebt(List<Loan> loans) {
        if (loans == null || loans.isEmpty()) {
            return List.of();
        }
        Map<Currency, BigDecimal> totals = new HashMap<>();
        for (Loan loan : loans) {
            if (loan != null && loan.active() && loan.principal() != null) {
                Currency currency = loan.principal().currency();
                BigDecimal remaining;
                if (loan.installments() != null && !loan.installments().isEmpty()) {
                    remaining = loan.installments().stream()
                            .filter(inst -> inst != null && !inst.paid() && inst.amount() != null)
                            .map(inst -> inst.amount().amount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                } else {
                    remaining = loan.principal().amount();
                }
                totals.merge(currency, remaining, BigDecimal::add);
            }
        }
        return totals.entrySet().stream()
                .map(entry -> new Money(entry.getValue(), entry.getKey()))
                .toList();
    }
}
