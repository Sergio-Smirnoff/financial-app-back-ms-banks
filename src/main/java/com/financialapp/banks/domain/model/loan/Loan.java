package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.loan.LoanAlreadyClosedException;
import com.financialapp.banks.domain.model.bank.BankName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Loan(
    LoanId id,
    UserId userId,
    BankName bankName,
    String name,
    Money principal,
    BigDecimal interestRate,
    int totalInstallments,
    int remainingInstallments,
    AmortizationType amortizationType,
    LocalDate startDate,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * Guards that the loan is still open.
     * @throws LoanAlreadyClosedException when the loan is not active.
     */
    public void ensureActive() {
        if (!active) {
            throw new LoanAlreadyClosedException(id.value().toString());
        }
    }

    /**
     * Records that one installment has been paid, returning a new instance
     * (immutability) with the remaining count decremented and the loan
     * deactivated once nothing remains. Refreshes {@code updatedAt}.
     * Does not re-check {@code active} — the caller must have already ensured it.
     */
    public Loan registerInstallmentPaid() {
        int remaining = remainingInstallments - 1;
        return new Loan(
                id, userId, bankName, name, principal, interestRate,
                totalInstallments, remaining, amortizationType, startDate,
                remaining > 0, createdAt, LocalDateTime.now());
    }
}
