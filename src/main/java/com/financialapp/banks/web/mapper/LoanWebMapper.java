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
                .bankNumber(loan.bankNumber().value())
                .userId(loan.userId().value())
                .name(loan.name())
                .principal(loan.principal().amount().toPlainString())
                .currency(loan.principal().currency().getCurrencyCode())
                .interestRate(loan.interestRate().toPlainString())
                .totalInstallments(loan.totalInstallments())
                .remainingInstallments(loan.remainingInstallments())
                .startDate(loan.startDate())
                .active(loan.active())
                .createdAt(loan.createdAt())
                .updatedAt(loan.updatedAt())
                .build();
    }
}
