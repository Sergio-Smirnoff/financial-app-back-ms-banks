package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.web.dto.response.LoanResponse;
import org.springframework.stereotype.Component;

@Component
public class LoanWebMapper {

    public LoanResponse toResponse(Loan loan) {
        if (loan == null) return null;
        return LoanResponse.builder()
                .id(loan.id().value())
                .bankName(loan.bankName().name())
                .userId(loan.userId().value())
                .name(loan.name())
                .principal(loan.details().principal())
                .currency(loan.details().currency())
                .interestRate(loan.details().interestRate())
                .totalInstallments(loan.details().totalInstallments())
                .remainingInstallments(loan.remainingInstallments())
                .startDate(loan.startDate())
                .active(loan.active())
                .createdAt(loan.createdAt())
                .updatedAt(loan.updatedAt())
                .build();
    }
}
