package com.financialapp.banks.mapper;

import com.financialapp.banks.model.dto.response.CardResponse;
import com.financialapp.banks.model.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    default CardResponse toResponse(Card card, String bankName) {
        if (card == null) return null;

        String displayName = String.format("%s %s %s ••%s",
                bankName != null ? bankName : "Unknown",
                card.getBrand(),
                card.getCardType(),
                card.getLast4Digits());

        return CardResponse.builder()
                .id(card.getId())
                .bankId(card.getBankId())
                .userId(card.getUserId())
                .displayName(displayName)
                .brand(card.getBrand())
                .cardType(card.getCardType())
                .behavior(card.getBehavior())
                .last4Digits(card.getLast4Digits())
                .expiringDate(card.getExpiringDate())
                .closingDay(card.getClosingDay())
                .dueDay(card.getDueDay())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}
