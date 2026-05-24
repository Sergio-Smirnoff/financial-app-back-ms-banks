package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.application.loan.command.DeleteLoanCommand;
import com.financialapp.banks.application.loan.usecase.DeleteLoanUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteLoanUseCaseImpl implements DeleteLoanUseCase {

    private final LoanRepository loanRepository;

    @Override
    @Transactional
    public void execute(DeleteLoanCommand command) {
        loanRepository.findByIdAndUserId(command.id(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", command.id().value().toString()));
        loanRepository.delete(command.id());
    }
}
