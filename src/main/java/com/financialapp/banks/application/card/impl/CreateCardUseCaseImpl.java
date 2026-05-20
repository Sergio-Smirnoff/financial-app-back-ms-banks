package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.command.CreateCardCommand;
import com.financialapp.banks.application.card.usecase.CreateCardUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardId;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateCardUseCaseImpl implements CreateCardUseCase {

    private final CardRepository cardRepository;
    private final BankRepository bankRepository;

    @Override
    @Transactional
    public Card execute(CreateCardCommand cmd) {
        bankRepository.findByName(cmd.bankName())
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + cmd.bankName()));

        if (cardRepository.existsByBankNameAndBrandAndTypeAndCardNumber(
                cmd.bankName(), cmd.brand(), cmd.cardType(), cmd.number())) {
            throw new BusinessException("A similar card with these 4 digits already exists for this bank");
        }

        Card card = new Card(
                new CardId(null),
                cmd.userId(),
                cmd.bankName(),
                new CardDetails(
                        cmd.brand(),
                        cmd.cardType(),
                        cmd.behavior(),
                        cmd.number(),
                        cmd.expiringDate(),
                        new CardBilling(cmd.closingDay(), cmd.dueDay())
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return cardRepository.save(card);
    }
}
