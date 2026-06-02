package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    List<Loan> findByUserId(UserId userId);
    List<Loan> findByBankNumber(BankNumber bankNumber);
    Optional<Loan> findById(LoanId id);
    Optional<Loan> findByIdAndUserId(LoanId id, UserId userId);
    int countByBankNumber(BankNumber bankNumber);
    List<Loan> findActiveWithUpcomingPayment(LocalDate from, LocalDate to);
    List<Loan> findWithUpcomingUnpaidInstallments(UserId userId, LocalDate from, LocalDate to);
    Loan save(Loan loan);
    void delete(LoanId id);
}
