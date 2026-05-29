package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.card.command.PayCardInstallmentCommand;
import com.financialapp.banks.domain.usecase.card.PayCardInstallmentUseCase;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.event.CardInstallmentPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PayCardInstallmentUseCaseImpl implements PayCardInstallmentUseCase {

    private final CardRepository cardRepository;
    private final AdjustBalanceUseCase adjustBalance;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public CardInstallment execute(PayCardInstallmentCommand cmd) {
        Card card = cardRepository.findByCardNumberAndUserId(cmd.cardNumber(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", cmd.cardNumber()));

        LocalDate paidDate = cmd.paidDate() != null ? cmd.paidDate() : LocalDate.now();
        CardInstallment paid = card.payInstallment(cmd.installmentId(), paidDate);
        cardRepository.save(card);

        adjustBalance.execute(new AdjustBalanceCommand(
                cmd.accountCbu(), new Money(paid.amount().amount().negate(), paid.amount().currency())));

        eventPublisher.publish(new CardInstallmentPaidEvent(
                cmd.userId(), cmd.accountCbu(),
                new Money(paid.amount().amount().negate(), paid.amount().currency()),
                paid.description(), paid.installmentNumber(), paid.totalInstallments(), paidDate));

        return paid;
    }
}
