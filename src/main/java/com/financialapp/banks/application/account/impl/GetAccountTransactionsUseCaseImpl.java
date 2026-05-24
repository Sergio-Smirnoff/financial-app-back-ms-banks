package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.usecase.GetAccountTransactionsUseCase;
import com.financialapp.banks.domain.exception.FinancesServiceException;
import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.domain.port.FinancesPort;
import com.financialapp.banks.domain.port.FinancesPort.TransactionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAccountTransactionsUseCaseImpl implements GetAccountTransactionsUseCase {

    private final FinancesPort financesPort;

    @Override
    public List<TransactionSummary> getRecent(String accountCbu, int limit) {
        try {
            return financesPort.getRecentTransactions(accountCbu, limit);
        } catch (InfrastructureException e) {
            throw new FinancesServiceException("getRecentTransactions", e.getMessage());
        }
    }

    @Override
    public List<TransactionSummary> getAll(String accountCbu) {
        try {
            return financesPort.getAllTransactions(accountCbu);
        } catch (InfrastructureException e) {
            throw new FinancesServiceException("getAllTransactions", e.getMessage());
        }
    }

    @Override
    public List<TransactionSummary> getFiltered(String accountCbu, LocalDate from, LocalDate to) {
        try {
            return financesPort.getFilteredTransactions(accountCbu, from, to);
        } catch (InfrastructureException e) {
            throw new FinancesServiceException("getFilteredTransactions", e.getMessage());
        }
    }
}
