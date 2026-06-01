package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.domain.usecase.loan.GetLoanUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetLoanUseCaseImpl implements GetLoanUseCase {

    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public Loan execute(LoanId id, UserId userId) {
        return loanRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", id.value().toString()));
    }
}
