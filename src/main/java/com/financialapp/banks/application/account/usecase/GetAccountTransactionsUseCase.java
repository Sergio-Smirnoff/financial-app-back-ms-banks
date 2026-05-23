package com.financialapp.banks.application.account.usecase;

import com.financialapp.banks.domain.port.FinancesPort.TransactionSummary;

import java.time.LocalDate;
import java.util.List;

public interface GetAccountTransactionsUseCase {
    List<TransactionSummary> getRecent(String accountCbu, int limit);
    List<TransactionSummary> getAll(String accountCbu);
    List<TransactionSummary> getFiltered(String accountCbu, LocalDate from, LocalDate to);
}
