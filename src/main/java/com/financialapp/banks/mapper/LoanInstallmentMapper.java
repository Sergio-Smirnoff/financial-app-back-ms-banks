package com.financialapp.banks.mapper;

import com.financialapp.banks.model.dto.response.LoanInstallmentResponse;
import com.financialapp.banks.model.entity.LoanInstallment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanInstallmentMapper {
    default LoanInstallmentResponse toResponse(LoanInstallment installment) {
        if (installment == null) return null;
        return LoanInstallmentResponse.builder()
                .id(installment.getId())
                .loanId(installment.getLoan().getId())
                .installmentNumber(installment.getInstallmentNumber())
                .amount(installment.getAmount())
                .dueDate(installment.getDueDate())
                .paid(installment.isPaid())
                .paidDate(installment.getPaidDate())
                .createdAt(installment.getCreatedAt())
                .updatedAt(installment.getUpdatedAt())
                .build();
    }
}
