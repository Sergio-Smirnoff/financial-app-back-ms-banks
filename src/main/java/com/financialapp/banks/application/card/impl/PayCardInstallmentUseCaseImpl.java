package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.impl.AdjustBalanceUseCaseImpl;
import com.financialapp.banks.domain.usecase.card.command.PayCardInstallmentCommand;
import com.financialapp.banks.domain.usecase.card.PayCardInstallmentUseCase;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.event.CardInstallmentPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PayCardInstallmentUseCaseImpl implements PayCardInstallmentUseCase {

    private final CardInstallmentRepository installmentRepository;
    private final CardRepository cardRepository;
    private final AdjustBalanceUseCaseImpl adjustBalance;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public CardInstallment execute(PayCardInstallmentCommand cmd) {
        cardRepository.findByCardNumberAndUserId(cmd.cardNumber(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", cmd.cardNumber()));

        CardInstallment installment = installmentRepository.findById(cmd.installmentId())
                .orElseThrow(() -> new ResourceNotFoundException("CardInstallment", cmd.installmentId().value().toString()));

        installment.ensureBelongsTo(cmd.cardNumber());

        LocalDate paidDate = cmd.paidDate() != null ? cmd.paidDate() : LocalDate.now();
        CardInstallment paid = installment.pay(paidDate);

        adjustBalance.execute(new AdjustBalanceCommand(
                cmd.accountCbu(), new Money(installment.amount().amount().negate(), installment.amount().currency())));

        CardInstallment saved = installmentRepository.save(paid);

        eventPublisher.publish(new CardInstallmentPaidEvent(
                cmd.userId(),
                cmd.accountCbu(),
                new Money(saved.amount().amount().negate(), saved.amount().currency()),
                saved.description(),
                saved.installmentNumber(),
                saved.totalInstallments(),
                paidDate
        ));

        return saved;
    }
}
