package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.infrastructure.persistence.entity.CardInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CardInstallmentPersistenceMapper {

    public CardInstallment toDomain(CardInstallmentJpaEntity entity) {
        if (entity == null) return null;
        Currency currency = Currency.getInstance(entity.getCurrency());
        return new CardInstallment(
                new CardInstallmentId(entity.getId()),
                entity.getCard().getCardNumber(),
                entity.getDescription(),
                new Money(entity.getTotalAmount(), currency),
                entity.getInstallmentNumber(),
                entity.getTotalInstallments(),
                new Money(entity.getAmount(), currency),
                entity.getDueDate(),
                entity.isPaid(),
                entity.getPaidDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CardInstallmentJpaEntity toJpa(CardInstallment installment, CardJpaEntity card) {
        if (installment == null) return null;
        return CardInstallmentJpaEntity.builder()
                .id(installment.id() != null ? installment.id().value() : null)
                .card(card)
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
    }

    public CardInstallmentJpaEntity merge(CardInstallmentJpaEntity existing, CardInstallment installment, CardJpaEntity card) {
        existing.setCard(card);
        existing.setDescription(installment.description());
        existing.setTotalAmount(installment.totalAmount().amount());
        existing.setCurrency(installment.totalAmount().currency().getCurrencyCode());
        existing.setInstallmentNumber(installment.installmentNumber());
        existing.setTotalInstallments(installment.totalInstallments());
        existing.setAmount(installment.amount().amount());
        existing.setDueDate(installment.dueDate());
        existing.setPaid(installment.paid());
        existing.setPaidDate(installment.paidDate());
        existing.setUpdatedAt(installment.updatedAt());
        return existing;
    }
}
