package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.web.dto.response.LoanInstallmentResponse;
import org.springframework.stereotype.Component;

@Component
public class LoanInstallmentWebMapper {

    public LoanInstallmentResponse toResponse(LoanInstallment installment) {
        if (installment == null) return null;
        return LoanInstallmentResponse.builder()
                .id(installment.id().value())
                .loanId(installment.loanId().value())
                .installmentNumber(installment.installmentNumber())
                .amount(installment.amount().amount().toPlainString())
                .dueDate(installment.dueDate())
                .paid(installment.paid())
                .paidDate(installment.paidDate())
                .createdAt(installment.createdAt())
                .updatedAt(installment.updatedAt())
                .build();
    }
}
