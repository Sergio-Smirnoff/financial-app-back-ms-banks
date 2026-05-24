package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.usecase.ListCardsUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankName;
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
    public List<Card> execute(UserId userId, BankName bankName) {
        if (bankName != null) {
            bankRepository.findByName(bankName)
                    .orElseThrow(() -> new ResourceNotFoundException("Bank", bankName.getDisplayName()));
            return cardRepository.findByBankName(bankName);
        }
        return cardRepository.findByUserId(userId);
    }
}
