package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.command.DeleteCardCommand;
import com.financialapp.banks.application.card.usecase.DeleteCardUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCardUseCaseImpl implements DeleteCardUseCase {

    private final CardRepository cardRepository;
    private final CardInstallmentRepository installmentRepository;

    @Override
    @Transactional
    public void execute(DeleteCardCommand command) {
        cardRepository.findByIdAndUserId(command.id(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + command.id().value()));

        if (installmentRepository.existsByCardIdAndUnpaid(command.id())) {
            throw new BusinessException("Cannot delete card with unpaid installments. Pay them first.");
        }

        cardRepository.delete(command.id());
    }
}
