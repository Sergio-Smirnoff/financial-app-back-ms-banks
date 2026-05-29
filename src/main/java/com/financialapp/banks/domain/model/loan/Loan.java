package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.loan.LoanAlreadyClosedException;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.service.LoanAmortization;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    List<LoanInstallment> installments,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public Loan {
        installments = installments == null ? List.of() : List.copyOf(installments);
    }

    /**
     * Factory: originates a new (unpersisted) loan and builds its full installment schedule
     * via {@link LoanAmortization}. The loan starts active with all installments unpaid; ids
     * are null until persisted. Cross-aggregate checks (account/bank existence) belong in the
     * use case, not here.
     */
    public static Loan originate(UserId userId, BankName bankName, String name, Money principal,
                                 BigDecimal interestRate, int totalInstallments,
                                 AmortizationType amortizationType, LocalDate startDate) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal perInstallment = LoanAmortization.frenchInstallment(
                principal.amount(), interestRate, totalInstallments);

        List<LoanInstallment> schedule = new ArrayList<>();
        for (int number = 1; number <= totalInstallments; number++) {
            schedule.add(new LoanInstallment(
                    new LoanInstallmentId(null),
                    new LoanId(null),
                    number,
                    new Money(perInstallment, principal.currency()),
                    startDate.plusMonths(number - 1),
                    false,
                    null,
                    now,
                    now));
        }
        return new Loan(new LoanId(null), userId, bankName, name, principal, interestRate,
                totalInstallments, totalInstallments, amortizationType, startDate, true, schedule, now, now);
    }

    /**
     * Guards that the loan is still open.
     * @throws LoanAlreadyClosedException when the loan is not active.
     */
    public void ensureActive() {
        if (!active) {
            throw new LoanAlreadyClosedException(id.value() == null ? "new" : id.value().toString());
        }
    }

    /**
     * Returns the installment with the given id.
     * @throws ResourceNotFoundException if no installment on this loan has that id.
     */
    public LoanInstallment installmentBy(LoanInstallmentId installmentId) {
        return installments.stream()
                .filter(installment -> installment.id().equals(installmentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("LoanInstallment",
                        installmentId.value() == null ? "new" : installmentId.value().toString()));
    }

    /**
     * Pays the installment identified by {@code installmentId} on {@code paidDate}, returning a
     * new aggregate instance with that installment paid, {@code remainingInstallments} decremented,
     * and the loan deactivated once nothing remains.
     * @throws LoanAlreadyClosedException when the loan is not active.
     * @throws com.financialapp.banks.domain.exception.loan.LoanInstallmentAlreadyPaidException when already paid.
     */
    public Loan payInstallment(LoanInstallmentId installmentId, LocalDate paidDate) {
        ensureActive();
        LoanInstallment target = installmentBy(installmentId);
        LoanInstallment paid = target.pay(paidDate);

        List<LoanInstallment> updated = new ArrayList<>(installments.size());
        for (LoanInstallment installment : installments) {
            updated.add(installment.id().equals(installmentId) ? paid : installment);
        }
        int remaining = remainingInstallments - 1;
        return new Loan(id, userId, bankName, name, principal, interestRate,
                totalInstallments, remaining, amortizationType, startDate,
                remaining > 0, updated, createdAt, LocalDateTime.now());
    }

    /**
     * Test/persistence helper: returns a copy whose installments carry the given ids (positional).
     * Used to simulate post-persistence id assignment.
     */
    public Loan withInstallmentIds(List<LoanInstallmentId> ids) {
        List<LoanInstallment> reIded = new ArrayList<>(installments.size());
        for (int index = 0; index < installments.size(); index++) {
            LoanInstallment current = installments.get(index);
            reIded.add(new LoanInstallment(ids.get(index), current.loanId(), current.installmentNumber(),
                    current.amount(), current.dueDate(), current.paid(), current.paidDate(),
                    current.createdAt(), current.updatedAt()));
        }
        return new Loan(id, userId, bankName, name, principal, interestRate, totalInstallments,
                remainingInstallments, amortizationType, startDate, active, reIded, createdAt, updatedAt);
    }
}
