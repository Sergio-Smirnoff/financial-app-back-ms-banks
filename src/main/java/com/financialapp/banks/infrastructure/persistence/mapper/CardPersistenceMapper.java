package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CardPersistenceMapper {

    public Card toDomain(CardJpaEntity entity, BankJpaEntity bank) {
        if (entity == null) return null;
        CardDetails details = new CardDetails(
                entity.getBrand(),
                entity.getCardType(),
                entity.getBehavior(),
                entity.getExpiringDate(),
                new CardBilling(entity.getClosingDay(), entity.getDueDay())
        );
        UserId userId = new UserId(entity.getUserId());
        BankName bankName = BankName.valueOf(bank.getName());
        return entity.getBehavior() == CardBehavior.INSTANT_PAYMENT
                ? new DebitCard(entity.getCardNumber(), userId, bankName, details, entity.getCreatedAt(), entity.getUpdatedAt())
                : new CreditCard(entity.getCardNumber(), userId, bankName, details, entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public CardJpaEntity toJpa(Card card, BankJpaEntity bank) {
        if (card == null) return null;
        CardDetails d = card.details();
        return CardJpaEntity.builder()
                .bankId(bank.getId())
                .userId(card.userId().value())
                .brand(d.brand())
                .cardType(d.cardType())
                .behavior(d.behavior())
                .cardNumber(card.cardNumber())
                .expiringDate(d.expiringDate())
                .closingDay(d.billing().closingDay())
                .dueDay(d.billing().dueDay())
                .createdAt(card.createdAt())
                .updatedAt(card.updatedAt())
                .build();
    }

    public CardJpaEntity merge(CardJpaEntity existing, Card card, BankJpaEntity bank) {
        CardDetails d = card.details();
        existing.setBankId(bank.getId());
        existing.setUserId(card.userId().value());
        existing.setBrand(d.brand());
        existing.setCardType(d.cardType());
        existing.setBehavior(d.behavior());
        existing.setCardNumber(card.cardNumber());
        existing.setExpiringDate(d.expiringDate());
        existing.setClosingDay(d.billing().closingDay());
        existing.setDueDay(d.billing().dueDay());
        existing.setUpdatedAt(card.updatedAt());
        return existing;
    }
}
