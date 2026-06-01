package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.card.command.IssueCardCommand;
import com.financialapp.banks.domain.usecase.card.IssueCardUseCase;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IssueCardUseCaseImpl implements IssueCardUseCase {

    private final CardRepository cardRepository;
    private final BankRepository bankRepository;

    @Override
    @Transactional
    public Card execute(IssueCardCommand cmd) {
        bankRepository.findByBankNumber(cmd.bankNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", cmd.bankNumber().value()));

        if (cardRepository.findByCardNumber(cmd.number()).isPresent()) {
            throw new ResourceAlreadyExistsException("Card", cmd.number());
        }

        CardDetails details = new CardDetails(
                cmd.brand(),
                cmd.cardType(),
                cmd.behavior(),
                cmd.expiringDate(),
                new CardBilling(cmd.closingDay(), cmd.dueDay())
        );

        LocalDateTime now = LocalDateTime.now();
        Card card = Card.create(cmd.number(), cmd.userId(), cmd.bankNumber(), details, now, now);

        return cardRepository.save(card);
    }
}
