package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.card.command.CancelCardCommand;
import com.financialapp.banks.domain.usecase.card.CancelCardUseCase;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ResourceConflictException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import java.util.Map;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelCardUseCaseImpl implements CancelCardUseCase {

    private final CardRepository cardRepository;

    @Override
    @Transactional
    public void execute(CancelCardCommand command) {
        Card card = cardRepository.findByCardNumberAndUserId(command.cardNumber(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", command.cardNumber()));

        if (card.hasUnpaidInstallments()) {
            throw new ResourceConflictException(
                DomainError.CARD_NOT_DELETABLE,
                "Cannot delete card '" + command.cardNumber() + "' — it has unpaid installments",
                Map.of("cardNumber", command.cardNumber()));
        }

        cardRepository.delete(command.cardNumber());
    }
}
