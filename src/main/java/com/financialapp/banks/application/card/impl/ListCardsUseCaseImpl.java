package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.card.ListCardsUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCardsUseCaseImpl implements ListCardsUseCase {

    private final CardRepository cardRepository;
    private final BankRepository bankRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Card> execute(UserId userId, BankNumber bankNumber) {
        if (bankNumber != null) {
            bankRepository.findByBankNumber(bankNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Bank", bankNumber.value()));
            return cardRepository.findByBankNumber(bankNumber);
        }
        return cardRepository.findByUserId(userId);
    }
}
