package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.impl.AdjustBalanceUseCaseImpl;
import com.financialapp.banks.application.card.command.PayCardInstallmentCommand;
import com.financialapp.banks.application.card.usecase.PayCardInstallmentUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.infrastructure.messaging.payload.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        cardRepository.findByIdAndUserId(cmd.cardId(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cmd.cardId().value()));

        CardInstallment installment = installmentRepository.findById(cmd.installmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found: " + cmd.installmentId().value()));

        if (!installment.cardId().equals(cmd.cardId())) {
            throw new BusinessException("Installment does not belong to the specified card");
        }
        if (installment.paid()) {
            throw new BusinessException("Installment is already paid");
        }

        if (!cmd.bypassBalance()) {
            adjustBalance.execute(new AdjustBalanceCommand(
                    cmd.accountId(), installment.amount().negate(), installment.currency()));
        }

        LocalDate paidDate = cmd.paidDate() != null ? cmd.paidDate() : LocalDate.now();
        CardInstallment paid = new CardInstallment(
                installment.id(),
                installment.cardId(),
                installment.description(),
                installment.totalAmount(),
                installment.currency(),
                installment.installmentNumber(),
                installment.totalInstallments(),
                installment.amount(),
                installment.dueDate(),
                true,
                paidDate,
                installment.createdAt(),
                LocalDateTime.now()
        );
        CardInstallment saved = installmentRepository.save(paid);

        eventPublisher.publish(PaymentEvent.builder()
                .userId(cmd.userId().value())
                .accountId(cmd.accountId().value())
                .amount(saved.amount().negate())
                .currency(saved.currency())
                .description("Card Installment: " + saved.description() +
                        " (" + saved.installmentNumber() + "/" + saved.totalInstallments() + ")")
                .date(paidDate)
                .build());

        return saved;
    }
}
