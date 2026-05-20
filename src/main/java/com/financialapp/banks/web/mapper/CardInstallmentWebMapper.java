package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.web.dto.response.CardInstallmentResponse;
import org.springframework.stereotype.Component;

@Component
public class CardInstallmentWebMapper {

    public CardInstallmentResponse toResponse(CardInstallment installment) {
        if (installment == null) return null;
        return CardInstallmentResponse.builder()
                .id(installment.id().value())
                .cardId(installment.cardId().value())
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
}
