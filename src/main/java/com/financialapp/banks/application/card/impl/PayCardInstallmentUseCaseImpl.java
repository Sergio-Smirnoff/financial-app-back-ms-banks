package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.card.command.PayCardInstallmentCommand;
import com.financialapp.banks.domain.usecase.card.PayCardInstallmentUseCase;
import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.card.CardInstallmentNotSupportedException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.service.CardInstallmentEventFactory;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        if (!(card instanceof CreditCard credit)) {
            throw new CardInstallmentNotSupportedException(cmd.cardNumber());
        }

        LocalDate paidDate = cmd.paidDate() != null ? cmd.paidDate() : LocalDate.now();
        List<CardInstallment> installments = new ArrayList<>(credit.installments());
        CardInstallment paid = payInstallment(installments, cmd.installmentId(), paidDate);

        CreditCard updated = new CreditCard(credit.cardNumber(), credit.userId(), credit.bankNumber(),
                credit.details(), credit.createdAt(), credit.updatedAt(), installments);
        cardRepository.save(updated);

        Money refund = new Money(paid.amount().amount().negate(), paid.amount().currency());
        adjustBalance.execute(new AdjustBalanceCommand(cmd.accountCbu(), refund));

        DomainEvent event = CardInstallmentEventFactory.installmentPaid(
                credit.userId(), cmd.accountCbu(), paid, paidDate);
        eventPublisher.publishAll(List.of(event));

        return paid;
    }

    /** Pays the installment with {@code installmentId} in place, returning the paid copy. */
    private static CardInstallment payInstallment(List<CardInstallment> installments,
                                                  CardInstallmentId installmentId,
                                                  LocalDate paidDate) {
        for (int index = 0; index < installments.size(); index++) {
            CardInstallment current = installments.get(index);
            if (current.id().equals(installmentId)) {
                CardInstallment paid = current.pay(paidDate);
                installments.set(index, paid);
                return paid;
            }
        }
        throw new ResourceNotFoundException("CardInstallment",
                installmentId.value() == null ? "new" : installmentId.value().toString());
    }
}
