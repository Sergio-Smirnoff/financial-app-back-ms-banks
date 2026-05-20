package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InstallmentRepository<T, ID, P> {
    List<T> findByParentId(P parentId);
    Optional<T> findById(ID id);
    boolean existsByParentIdAndUnpaid(P parentId);
    boolean existsByParentIdAndDescriptionAndAmountAndDueDate(P parentId, String description, Money amount, LocalDate dueDate);
    T save(T installment);
    List<T> saveAll(List<T> installments);
    List<T> findUpcomingUnpaid(UserId userId, LocalDate from, LocalDate to);
}
