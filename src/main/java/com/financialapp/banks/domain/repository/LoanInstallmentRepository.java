package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanInstallmentRepository extends InstallmentRepository<LoanInstallment, LoanInstallmentId, LoanId> {
    List<LoanInstallment> findByLoanId(LoanId loanId);
    Optional<LoanInstallment> findById(LoanInstallmentId id);
    List<LoanInstallment> findUpcomingUnpaid(LocalDate from, LocalDate to);
    LoanInstallment save(LoanInstallment installment);
    List<LoanInstallment> saveAll(List<LoanInstallment> installments);
}
