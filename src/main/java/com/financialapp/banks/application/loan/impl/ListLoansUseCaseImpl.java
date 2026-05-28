package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.domain.usecase.loan.ListLoansUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListLoansUseCaseImpl implements ListLoansUseCase {

    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Loan> execute(UserId userId, BankName bankName) {
        return bankName != null
                ? loanRepository.findByBankName(bankName)
                : loanRepository.findByUserId(userId);
    }
}
