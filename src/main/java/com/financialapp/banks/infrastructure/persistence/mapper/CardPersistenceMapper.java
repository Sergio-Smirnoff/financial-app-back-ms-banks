package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;

@Component
public class CardPersistenceMapper {

    public Card toDomain(CardJpaEntity entity, BankJpaEntity bank) {
        if (entity == null) return null;
        CardDetails details = new CardDetails(
                entity.getBrand(),
                entity.getCardType(),
                entity.getBehavior(),
                YearMonth.from(entity.getExpiringDate()),
                new CardBilling(entity.getClosingDay(), entity.getDueDay())
        );
        UserId userId = new UserId(entity.getUserId());
        BankName bankName = BankName.valueOf(bank.getName());
        CardNumber cardNumber = new CardNumber(entity.getCardNumber());
        Card card = entity.getBehavior() == CardBehavior.INSTANT_PAYMENT
                ? new DebitCard(cardNumber, userId, bankName, details, entity.getCreatedAt(), entity.getUpdatedAt())
                : new CreditCard(cardNumber, userId, bankName, details, entity.getCreatedAt(), entity.getUpdatedAt());

        List<CardInstallment> installments = entity.getInstallments().stream()
                .sorted(Comparator.comparing(CardInstallmentJpaEntity::getDueDate))
                .map(child -> {
                    Currency currency = Currency.getInstance(child.getCurrency());
                    return new CardInstallment(
                            new CardInstallmentId(child.getId()),
                            entity.getCardNumber(),
                            child.getDescription(),
                            new Money(child.getTotalAmount(), currency),
                            child.getInstallmentNumber(),
                            child.getTotalInstallments(),
                            new Money(child.getAmount(), currency),
                            child.getDueDate(),
                            child.isPaid(),
                            child.getPaidDate(),
                            child.getCreatedAt(),
                            child.getUpdatedAt());
                })
                .toList();
        card.restoreInstallments(installments);
        return card;
    }

    public CardJpaEntity toJpa(Card card, BankJpaEntity bank) {
        if (card == null) return null;
        CardDetails d = card.details();
        CardJpaEntity entity = CardJpaEntity.builder()
                .bankId(bank.getId())
                .userId(card.userId().value())
                .brand(d.brand())
                .cardType(d.cardType())
                .behavior(d.behavior())
                .cardNumber(card.cardNumber().value())
                .expiringDate(d.expiringDate().atEndOfMonth())
                .closingDay(d.billing().closingDay())
                .dueDay(d.billing().dueDay())
                .createdAt(card.createdAt())
                .updatedAt(card.updatedAt())
                .build();
        syncInstallments(entity, card);
        return entity;
    }

    public CardJpaEntity merge(CardJpaEntity existing, Card card, BankJpaEntity bank) {
        CardDetails d = card.details();
        existing.setBankId(bank.getId());
        existing.setUserId(card.userId().value());
        existing.setBrand(d.brand());
        existing.setCardType(d.cardType());
        existing.setBehavior(d.behavior());
        existing.setCardNumber(card.cardNumber().value());
        existing.setExpiringDate(d.expiringDate().atEndOfMonth());
        existing.setClosingDay(d.billing().closingDay());
        existing.setDueDay(d.billing().dueDay());
        existing.setUpdatedAt(card.updatedAt());
        syncInstallments(existing, card);
        return existing;
    }

    private void syncInstallments(CardJpaEntity cardEntity, Card card) {
        cardEntity.getInstallments().clear();
        for (CardInstallment installment : card.installments()) {
            CardInstallmentJpaEntity child = CardInstallmentJpaEntity.builder()
                    .id(installment.id() != null ? installment.id().value() : null)
                    .card(cardEntity)
                    .description(installment.description())
                    .totalAmount(installment.totalAmount().amount())
                    .currency(installment.totalAmount().currency().getCurrencyCode())
                    .installmentNumber(installment.installmentNumber())
                    .totalInstallments(installment.totalInstallments())
                    .amount(installment.amount().amount())
                    .dueDate(installment.dueDate())
                    .paid(installment.paid())
                    .paidDate(installment.paidDate())
                    .createdAt(installment.createdAt())
                    .updatedAt(installment.updatedAt())
                    .build();
            cardEntity.getInstallments().add(child);
        }
    }
}
