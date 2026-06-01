package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.card.command.RegisterCardExpenseCommand;
import com.financialapp.banks.domain.usecase.card.RegisterCardExpenseUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.card.CardInstallmentNotSupportedException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        if (!(card instanceof CreditCard credit)) {
            throw new CardInstallmentNotSupportedException(cmd.cardNumber());
        }

        List<CardInstallment> installments = new ArrayList<>(credit.installments());
        installments.addAll(CardInstallment.schedule(
                credit.cardNumber().value(), cmd.description(), cmd.amount(),
                cmd.totalInstallments(), cmd.firstDueDate()));

        CreditCard updated = new CreditCard(credit.cardNumber(), credit.userId(), credit.bankNumber(),
                credit.details(), credit.createdAt(), credit.updatedAt(), installments);
        CreditCard saved = (CreditCard) cardRepository.save(updated);
        return saved.installments().stream()
                .filter(installment -> installment.description().equals(cmd.description())
                        && !installment.dueDate().isBefore(cmd.firstDueDate()))
                .toList();
    }
}
