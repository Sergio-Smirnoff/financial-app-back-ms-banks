package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.command.CreateCardCommand;
import com.financialapp.banks.application.card.usecase.CreateCardUseCase;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
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
                .orElseThrow(() -> new ResourceNotFoundException("Bank", cmd.bankName().getDisplayName()));

        if (cardRepository.existsByBankNameAndBrandAndTypeAndCardNumber(
                cmd.bankName(), cmd.brand(), cmd.cardType(), cmd.number())) {
            throw new ResourceAlreadyExistsException("Card", cmd.number() + " at " + cmd.bankName().getDisplayName());
        }

        CardDetails details = new CardDetails(
                cmd.brand(),
                cmd.cardType(),
                cmd.behavior(),
                cmd.expiringDate(),
                new CardBilling(cmd.closingDay(), cmd.dueDay())
        );

        LocalDateTime now = LocalDateTime.now();
        Card card = cmd.behavior() == CardBehavior.INSTANT_PAYMENT
                ? new DebitCard(cmd.number(), cmd.userId(), cmd.bankName(), details, now, now)
                : new CreditCard(cmd.number(), cmd.userId(), cmd.bankName(), details, now, now);

        return cardRepository.save(card);
    }
}
