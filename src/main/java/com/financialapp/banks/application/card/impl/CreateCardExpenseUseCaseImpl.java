package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.application.card.usecase.CreateCardExpenseUseCase;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateCardExpenseUseCaseImpl implements CreateCardExpenseUseCase {

    private final CardInstallmentRepository installmentRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public List<CardInstallment> execute(CreateCardExpenseCommand cmd) {
        var card = cardRepository.findByIdAndUserId(cmd.cardId(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cmd.cardId().value()));

        if (card.details().behavior() == CardBehavior.INSTANT_PAYMENT) {
            throw new BusinessException("Instant payment cards do not support installment-based expenses");
        }

        BigDecimal perInstallment = cmd.amount().amount()
                .divide(BigDecimal.valueOf(cmd.totalInstallments()), 2, RoundingMode.HALF_UP);
        BigDecimal lastInstallment = cmd.amount().amount()
                .subtract(perInstallment.multiply(BigDecimal.valueOf(cmd.totalInstallments() - 1)));

        List<CardInstallment> installments = new ArrayList<>();
        for (int i = 1; i <= cmd.totalInstallments(); i++) {
            BigDecimal amount = (i == cmd.totalInstallments()) ? lastInstallment : perInstallment;
            installments.add(new CardInstallment(
                    new CardInstallmentId(null),
                    cmd.cardId(),
                    cmd.description(),
                    cmd.amount(),
                    i,
                    cmd.totalInstallments(),
                    new Money(amount, cmd.amount().currency()),
                    cmd.firstDueDate().plusMonths(i - 1),
                    false,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            ));
        }

        return installmentRepository.saveAll(installments);
    }
}
