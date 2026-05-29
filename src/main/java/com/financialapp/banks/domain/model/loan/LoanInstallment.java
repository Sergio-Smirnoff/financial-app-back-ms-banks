package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.loan.LoanInstallmentAlreadyPaidException;
import com.financialapp.banks.domain.exception.loan.LoanInstallmentMismatchException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoanInstallment(
    LoanInstallmentId id,
    LoanId loanId,
    int installmentNumber,
    Money amount,
    LocalDate dueDate,
    boolean paid,
    LocalDate paidDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * Guards that this installment belongs to the given loan.
     * @throws LoanInstallmentMismatchException when the installment's loan differs from {@code expectedLoanId}.
     */
    public void ensureBelongsTo(LoanId expectedLoanId) {
        if (!loanId.equals(expectedLoanId)) {
            throw new LoanInstallmentMismatchException(
                    id.value().toString(), expectedLoanId.value().toString());
        }
    }

    /**
     * Marks this installment as paid on {@code paidDate}, returning a new instance
     * (immutability). Refreshes {@code updatedAt}.
     * @throws LoanInstallmentAlreadyPaidException when the installment is already paid.
     */
    public LoanInstallment pay(LocalDate paidDate) {
        if (paid) {
            throw new LoanInstallmentAlreadyPaidException(id.value().toString());
        }
        return new LoanInstallment(
                id, loanId, installmentNumber, amount, dueDate,
                true, paidDate, createdAt, LocalDateTime.now());
    }
}
