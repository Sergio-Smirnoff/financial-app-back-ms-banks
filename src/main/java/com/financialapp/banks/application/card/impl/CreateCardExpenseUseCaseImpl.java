package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.domain.usecase.card.CreateCardExpenseUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateCardExpenseUseCaseImpl implements CreateCardExpenseUseCase {

    private final CardInstallmentRepository installmentRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public List<CardInstallment> execute(CreateCardExpenseCommand cmd) {
        var card = cardRepository.findByCardNumberAndUserId(cmd.cardNumber(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", cmd.cardNumber()));

        card.ensureSupportsInstallments();

        List<CardInstallment> installments = CardInstallment.schedule(
                cmd.cardNumber(), cmd.description(), cmd.amount(), cmd.totalInstallments(), cmd.firstDueDate());

        return installmentRepository.saveAll(installments);
    }
}
