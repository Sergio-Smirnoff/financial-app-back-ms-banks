package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.usecase.GetAccountTransactionsUseCase;
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
        return financesPort.getRecentTransactions(accountCbu, limit);
    }

    @Override
    public List<TransactionSummary> getAll(String accountCbu) {
        return financesPort.getAllTransactions(accountCbu);
    }

    @Override
    public List<TransactionSummary> getFiltered(String accountCbu, LocalDate from, LocalDate to) {
        return financesPort.getFilteredTransactions(accountCbu, from, to);
    }
}
