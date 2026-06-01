package com.financialapp.banks.domain.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;


public abstract class Installment {
    int installmentNumber;
    Money amount;
    boolean paid;
    LocalDate dueDate;
    LocalDate paidDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
