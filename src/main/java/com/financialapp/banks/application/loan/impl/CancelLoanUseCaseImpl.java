package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.domain.usecase.loan.command.CancelLoanCommand;
import com.financialapp.banks.domain.usecase.loan.CancelLoanUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelLoanUseCaseImpl implements CancelLoanUseCase {

    private final LoanRepository loanRepository;

    @Override
    @Transactional
    public void execute(CancelLoanCommand command) {
        loanRepository.findByIdAndUserId(command.id(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", command.id().value().toString()));
        loanRepository.delete(command.id());
    }
}
