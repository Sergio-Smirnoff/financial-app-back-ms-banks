package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.card.CardInstallmentAlreadyPaidException;
import com.financialapp.banks.domain.exception.card.CardInstallmentMismatchException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CardInstallment(
    CardInstallmentId id,
    String cardNumber,
    String description,
    Money totalAmount,
    int installmentNumber,
    int totalInstallments,
    Money amount,
    LocalDate dueDate,
    boolean paid,
    LocalDate paidDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * Guards that this installment belongs to the given card.
     * @throws CardInstallmentMismatchException when this installment's card differs from {@code expectedCardNumber}.
     */
    public void ensureBelongsTo(String expectedCardNumber) {
        if (!cardNumber.equals(expectedCardNumber)) {
            throw new CardInstallmentMismatchException(id.value().toString(), expectedCardNumber);
        }
    }

    /**
     * Marks this installment as paid on {@code paidDate}, returning a new instance
     * (immutability). Refreshes {@code updatedAt}.
     * @throws CardInstallmentAlreadyPaidException when the installment is already paid.
     */
    public CardInstallment pay(LocalDate paidDate) {
        if (paid) {
            throw new CardInstallmentAlreadyPaidException(id.value().toString());
        }
        return new CardInstallment(
                id, cardNumber, description, totalAmount, installmentNumber,
                totalInstallments, amount, dueDate, true, paidDate, createdAt, LocalDateTime.now());
    }

    /**
     * Splits {@code total} into {@code totalInstallments} unpaid installments.
     * Each installment is {@code total / n} rounded to scale 2 (HALF_UP); the
     * rounding remainder lands on the LAST installment so the sum equals {@code total}.
     * Due dates run monthly from {@code firstDueDate}. Ids are {@code new CardInstallmentId(null)}
     * (unpersisted) and timestamps are stamped now.
     */
    public static List<CardInstallment> schedule(String cardNumber, String description, Money total,
                                                 int totalInstallments, LocalDate firstDueDate) {
        BigDecimal perInstallment = total.amount()
                .divide(BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_UP);
        BigDecimal lastInstallment = total.amount()
                .subtract(perInstallment.multiply(BigDecimal.valueOf(totalInstallments - 1)));

        List<CardInstallment> installments = new ArrayList<>();
        for (int i = 1; i <= totalInstallments; i++) {
            BigDecimal amount = (i == totalInstallments) ? lastInstallment : perInstallment;
            installments.add(new CardInstallment(
                    new CardInstallmentId(null),
                    cardNumber,
                    description,
                    total,
                    i,
                    totalInstallments,
                    new Money(amount, total.currency()),
                    firstDueDate.plusMonths(i - 1),
                    false,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            ));
        }
        return installments;
    }
}
