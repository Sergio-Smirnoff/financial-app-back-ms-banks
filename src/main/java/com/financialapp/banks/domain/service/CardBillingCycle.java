package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.model.card.BillingPeriod;
import com.financialapp.banks.domain.model.card.CardBilling;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class CardBillingCycle {

    public BillingPeriod currentPeriod(CardBilling billing, LocalDate today) {
        Objects.requireNonNull(billing, "billing must not be null");
        Objects.requireNonNull(today, "today must not be null");

        YearMonth closingYm = YearMonth.of(today.getYear(), today.getMonth());
        int clampedClosingDay = Math.min(billing.closingDay(), closingYm.lengthOfMonth());
        LocalDate closingDate = LocalDate.of(today.getYear(), today.getMonth(), clampedClosingDay);

        YearMonth dueYm = (billing.dueDay() >= billing.closingDay()) ? closingYm : closingYm.plusMonths(1);
        int clampedDueDay = Math.min(billing.dueDay(), dueYm.lengthOfMonth());
        LocalDate dueDate = LocalDate.of(dueYm.getYear(), dueYm.getMonth(), clampedDueDay);

        boolean statementOpen = !today.isAfter(closingDate);

        return new BillingPeriod(closingDate, dueDate, statementOpen);
    }
}
