package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.card.command.RegisterCardExpenseCommand;
import com.financialapp.banks.domain.usecase.card.RegisterCardExpenseUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegisterCardExpenseUseCaseImpl implements RegisterCardExpenseUseCase {

    private final CardRepository cardRepository;

    @Override
    @Transactional
    public List<CardInstallment> execute(RegisterCardExpenseCommand cmd) {
        Card card = cardRepository.findByCardNumberAndUserId(cmd.cardNumber(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", cmd.cardNumber()));
        card.registerExpense(cmd.description(), cmd.amount(), cmd.totalInstallments(), cmd.firstDueDate());
        Card saved = cardRepository.save(card);
        return saved.installments().stream()
                .filter(installment -> installment.description().equals(cmd.description())
                        && !installment.dueDate().isBefore(cmd.firstDueDate()))
                .toList();
    }
}
