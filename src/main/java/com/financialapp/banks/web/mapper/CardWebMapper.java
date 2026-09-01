package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.card.BillingPeriod;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.service.CardBillingCycle;
import com.financialapp.banks.domain.service.CreditLimitUsage;
import com.financialapp.banks.web.dto.response.CardResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class CardWebMapper {

    private final CardBillingCycle billingCycle = new CardBillingCycle();
    private final CreditLimitUsage creditLimitUsage = new CreditLimitUsage();

    public CardResponse toResponse(Card card) {
        if (card == null) return null;
        String displayName = String.format("%s %s %s ••%s",
                card.bankNumber().value(),
                card.details().brand(),
                card.details().cardType(),
                card.cardNumber().last4());

        BillingPeriod period = billingCycle.currentPeriod(card.details().billing(), LocalDate.now());

        List<CardInstallment> installments = card instanceof CreditCard credit ? credit.installments() : List.of();
        List<CardInstallment> currentPeriodInstallments = installments.stream()
                .filter(i -> !i.dueDate().isAfter(period.dueDate()))
                .toList();

        Money usedAmountMoney = creditLimitUsage.usedAmount(currentPeriodInstallments);
        BigDecimal usedPercentValue = creditLimitUsage.usedPercent(card.details().creditLimit(), usedAmountMoney);

        String creditLimit = card.details().creditLimit() != null ? card.details().creditLimit().amount().toPlainString() : null;
        String usedAmount = usedAmountMoney != null ? usedAmountMoney.amount().toPlainString() : null;
        String usedPercent = usedPercentValue != null ? usedPercentValue.toPlainString() : "0.00";

        return CardResponse.builder()
                .bankNumber(card.bankNumber().value())
                .userId(card.userId().value())
                .displayName(displayName)
                .brand(card.details().brand())
                .cardType(card.details().cardType())
                .behavior(card.details().behavior())
                .cardNumber(card.cardNumber().value())
                .expiringDate(card.details().expiringDate())
                .closingDay(card.details().billing().closingDay())
                .dueDay(card.details().billing().dueDay())
                .creditLimit(creditLimit)
                .usedAmount(usedAmount)
                .usedPercent(usedPercent)
                .closingDate(period.closingDate())
                .dueDate(period.dueDate())
                .statementOpen(period.statementOpen())
                .createdAt(card.createdAt())
                .updatedAt(card.updatedAt())
                .build();
    }
}
