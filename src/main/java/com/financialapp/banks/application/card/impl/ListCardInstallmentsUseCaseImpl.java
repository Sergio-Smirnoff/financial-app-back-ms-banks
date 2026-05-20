package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.usecase.ListCardInstallmentsUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.CardId;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCardInstallmentsUseCaseImpl implements ListCardInstallmentsUseCase {

    private final CardInstallmentRepository installmentRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CardInstallment> execute(CardId cardId, UserId userId) {
        cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId.value()));
        return installmentRepository.findByCardId(cardId);
    }
}
