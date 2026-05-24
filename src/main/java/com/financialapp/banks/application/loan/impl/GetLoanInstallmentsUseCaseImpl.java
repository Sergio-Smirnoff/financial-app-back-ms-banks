package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.application.loan.usecase.GetLoanInstallmentsUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.repository.LoanInstallmentRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetLoanInstallmentsUseCaseImpl implements GetLoanInstallmentsUseCase {

    private final LoanInstallmentRepository installmentRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LoanInstallment> execute(LoanId loanId, UserId userId) {
        loanRepository.findByIdAndUserId(loanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId.value().toString()));
        return installmentRepository.findByLoanId(loanId);
    }
}
