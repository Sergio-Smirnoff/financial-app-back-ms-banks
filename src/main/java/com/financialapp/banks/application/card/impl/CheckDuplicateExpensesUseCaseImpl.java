package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.application.card.usecase.CheckDuplicateExpensesUseCase;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CheckDuplicateExpensesUseCaseImpl implements CheckDuplicateExpensesUseCase {

    private final CardInstallmentRepository installmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Integer> execute(String cardNumber, List<CreateCardExpenseCommand> expenses) {
        return IntStream.range(0, expenses.size())
                .filter(i -> {
                    CreateCardExpenseCommand cmd = expenses.get(i);
                    return installmentRepository.existsByCardNumberAndDescriptionAndAmountAndDueDate(
                            cardNumber, cmd.description(), cmd.amount(), cmd.firstDueDate());
                })
                .boxed()
                .toList();
    }
}
