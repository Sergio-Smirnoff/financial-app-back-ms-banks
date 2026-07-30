package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.card.CardInstallment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CreditLimitUsage {

    public Money usedAmount(List<CardInstallment> currentPeriodInstallments) {
        if (currentPeriodInstallments == null || currentPeriodInstallments.isEmpty()) {
            return null;
        }
        Money total = null;
        for (CardInstallment installment : currentPeriodInstallments) {
            if (!installment.paid()) {
                if (total == null) {
                    total = installment.amount();
                } else {
                    total = total.add(installment.amount());
                }
            }
        }
        return total;
    }

    public BigDecimal usedPercent(Money creditLimit, Money usedAmount) {
        if (creditLimit == null || creditLimit.amount() == null || creditLimit.amount().signum() == 0 || usedAmount == null || usedAmount.amount() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return usedAmount.amount()
                .multiply(new BigDecimal("100"))
                .divide(creditLimit.amount(), 2, RoundingMode.HALF_UP);
    }
}
