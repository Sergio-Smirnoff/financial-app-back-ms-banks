package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.command.UpdateCardCommand;
import com.financialapp.banks.application.card.usecase.UpdateCardUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.card.CardExpiredException;
import com.financialapp.banks.domain.exception.card.CardInvalidTypeException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class UpdateCardUseCaseImpl implements UpdateCardUseCase {

    private final CardRepository cardRepository;

    @Override
    @Transactional
    public Card execute(UpdateCardCommand cmd) {
        Card card = cardRepository.findByCardNumberAndUserId(cmd.cardNumber(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", cmd.cardNumber()));

        CardDetails current = card.details();
        YearMonth newExpiry = cmd.expiringDate() != null ? cmd.expiringDate() : current.expiringDate();
        if (cmd.expiringDate() != null && cmd.expiringDate().isBefore(YearMonth.now())) {
            throw new CardExpiredException(cmd.cardNumber(), cmd.expiringDate().toString());
        }
        int newClosing = cmd.closingDay() != null ? cmd.closingDay() : current.billing().closingDay();
        int newDue = cmd.dueDay() != null ? cmd.dueDay() : current.billing().dueDay();

        CardDetails updated = new CardDetails(
                current.brand(),
                current.cardType(),
                current.behavior(),
                newExpiry,
                new CardBilling(newClosing, newDue));

        Card updatedCard = switch (card) {
            case CreditCard c -> new CreditCard(c.cardNumber(), c.userId(), c.bankName(),
                    updated, c.createdAt(), LocalDateTime.now());
            case DebitCard d -> new DebitCard(d.cardNumber(), d.userId(), d.bankName(),
                    updated, d.createdAt(), LocalDateTime.now());
            default -> throw new CardInvalidTypeException(card.getClass().getSimpleName());
        };

        return cardRepository.save(updatedCard);
    }
}
