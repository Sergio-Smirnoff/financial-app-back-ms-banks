package com.financialapp.banks.mapper;

import com.financialapp.banks.model.dto.response.CardInstallmentResponse;
import com.financialapp.banks.model.entity.CardInstallment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardInstallmentMapper {

    default CardInstallmentResponse toResponse(CardInstallment installment) {
        if (installment == null) return null;
        return CardInstallmentResponse.builder()
                .id(installment.getId())
                .cardId(installment.getCard().getId())
                .description(installment.getDescription())
                .totalAmount(installment.getTotalAmount())
                .currency(installment.getCurrency())
                .installmentNumber(installment.getInstallmentNumber())
                .totalInstallments(installment.getTotalInstallments())
                .amount(installment.getAmount())
                .dueDate(installment.getDueDate())
                .paid(installment.isPaid())
                .paidDate(installment.getPaidDate())
                .createdAt(installment.getCreatedAt())
                .updatedAt(installment.getUpdatedAt())
                .build();
    }
}
