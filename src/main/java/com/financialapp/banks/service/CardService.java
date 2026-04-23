package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.kafka.event.PaymentEvent;
import com.financialapp.banks.kafka.producer.BanksEventProducer;
import com.financialapp.banks.mapper.CardMapper;
import com.financialapp.banks.model.dto.request.CardRequest;
import com.financialapp.banks.model.dto.response.CardResponse;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.model.entity.Card;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import com.financialapp.banks.repository.CardInstallmentRepository;
import com.financialapp.banks.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;
    private final CardInstallmentRepository cardInstallmentRepository;
    private final CardMapper cardMapper;
    private final BanksEventProducer eventProducer;

    @Transactional(readOnly = true)
    public List<CardResponse> list(Long userId, Long bankId) {
        List<Card> cards;
        if (bankId != null) {
            // Verify bank belongs to user
            bankRepository.findByIdAndUserId(bankId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + bankId));
            cards = cardRepository.findByBankId(bankId);
        } else {
            cards = cardRepository.findByUserId(userId);
        }
        return cards.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CardResponse get(Long id, Long userId) {
        Card card = cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
        return mapToResponse(card);
    }

    @Transactional
    public CardResponse create(Long userId, CardRequest request) {
        Bank bank = bankRepository.findByIdAndUserId(request.bankId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + request.bankId()));

        if (cardRepository.existsByBankIdAndBrandAndCardTypeAndLast4Digits(
                request.bankId(), request.brand(), request.cardType(), request.last4Digits())) {
            throw new BusinessException("A similar card with these 4 digits already exists for this bank");
        }

        Card card = Card.builder()
                .bankId(request.bankId())
                .userId(userId)
                .brand(request.brand())
                .cardType(request.cardType())
                .behavior(request.behavior())
                .last4Digits(request.last4Digits())
                .expiringDate(request.expiringDate())
                .closingDay(request.closingDay())
                .dueDay(request.dueDay())
                .build();

        return mapToResponse(cardRepository.save(card));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Card card = cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
        
        // Check for unpaid installments
        boolean hasUnpaid = cardInstallmentRepository.existsByCardIdAndPaidFalse(id);
        if (hasUnpaid) {
            throw new BusinessException("Cannot delete card with unpaid installments. Pay them first.");
        }
        
        cardRepository.delete(card);
    }

    @Transactional
    public void recordInstantExpense(Long cardId, Long userId, BigDecimal amount, String description, LocalDate date, Long accountId) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));

        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        PaymentEvent event = new PaymentEvent(
                userId,
                accountId,
                amount.negate(),
                account.getCurrency(),
                description,
                date
        );
        eventProducer.sendPaymentEvent(event);
    }

    private CardResponse mapToResponse(Card card) {
        Bank bank = bankRepository.findById(card.getBankId()).orElse(null);
        String bankName = bank != null ? bank.getName() : "Unknown";
        return cardMapper.toResponse(card, bankName);
    }
}
