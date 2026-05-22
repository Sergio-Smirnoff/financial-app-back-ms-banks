package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.usecase.GetCardUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCardUseCaseImpl implements GetCardUseCase {

    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public Card execute(String cardNumber, UserId userId) {
        return cardRepository.findByCardNumberAndUserId(cardNumber, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNumber));
    }
}
