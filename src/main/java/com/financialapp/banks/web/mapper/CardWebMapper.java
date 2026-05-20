package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.web.dto.response.CardResponse;
import org.springframework.stereotype.Component;

@Component
public class CardWebMapper {

    public CardResponse toResponse(Card card) {
        if (card == null) return null;
        String displayName = String.format("%s %s %s ••%s",
                card.bankName().getDisplayName(),
                card.details().brand(),
                card.details().cardType(),
                card.details().cardNumber());
        return CardResponse.builder()
                .id(card.id().value())
                .bankName(card.bankName().name())
                .userId(card.userId().value())
                .displayName(displayName)
                .brand(card.details().brand())
                .cardType(card.details().cardType())
                .behavior(card.details().behavior())
                .last4Digits(card.details().cardNumber())
                .expiringDate(card.details().expiringDate())
                .closingDay(card.details().billing().closingDay())
                .dueDay(card.details().billing().dueDay())
                .createdAt(card.createdAt())
                .updatedAt(card.updatedAt())
                .build();
    }
}
