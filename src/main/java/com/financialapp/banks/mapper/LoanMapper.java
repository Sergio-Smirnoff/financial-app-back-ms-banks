package com.financialapp.banks.mapper;

import com.financialapp.banks.model.dto.response.LoanResponse;
import com.financialapp.banks.model.entity.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanMapper {
    default LoanResponse toResponse(Loan loan) {
        if (loan == null) return null;
        return LoanResponse.builder()
                .id(loan.getId())
                .bankId(loan.getBankId())
                .userId(loan.getUserId())
                .name(loan.getName())
                .principal(loan.getPrincipal())
                .currency(loan.getCurrency())
                .interestRate(loan.getInterestRate())
                .totalInstallments(loan.getTotalInstallments())
                .remainingInstallments(loan.getRemainingInstallments())
                .startDate(loan.getStartDate())
                .active(loan.isActive())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
