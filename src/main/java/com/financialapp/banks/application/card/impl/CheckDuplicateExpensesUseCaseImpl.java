package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.card.command.RegisterCardExpenseCommand;
import com.financialapp.banks.domain.usecase.card.CheckDuplicateExpensesUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CheckDuplicateExpensesUseCaseImpl implements CheckDuplicateExpensesUseCase {

    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Integer> execute(String cardNumber, UserId userId, List<RegisterCardExpenseCommand> expenses) {
        Card card = cardRepository.findByCardNumberAndUserId(cardNumber, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardNumber));

        return IntStream.range(0, expenses.size())
                .filter(expenseIndex -> {
                    RegisterCardExpenseCommand candidate = expenses.get(expenseIndex);
                    return card.hasInstallmentMatching(
                            candidate.description(), candidate.amount(), candidate.firstDueDate());
                })
                .boxed()
                .toList();
    }
}
