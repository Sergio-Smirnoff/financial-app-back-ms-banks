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
    private final CardMapper cardMapper;
    private final BanksEventProducer eventProducer;

    @Transactional(readOnly = true)
    public List<CardResponse> list(Long userId, Long accountId) {
        List<Card> cards;
        if (accountId != null) {
            // Verify account belongs to user
            accountRepository.findByIdAndUserId(accountId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
            cards = cardRepository.findByAccountId(accountId);
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
        Account account = accountRepository.findByIdAndUserId(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.accountId()));

        if (cardRepository.existsByAccountIdAndBrandAndCardTypeAndLast4Digits(
                request.accountId(), request.brand(), request.cardType(), request.last4Digits())) {
            throw new BusinessException("A similar card with these 4 digits already exists for this account");
        }

        Card card = Card.builder()
                .accountId(request.accountId())
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
        cardRepository.delete(card);
    }

    @Transactional
    public void recordInstantExpense(Long cardId, Long userId, BigDecimal amount, String description, LocalDate date) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));

        Account account = accountRepository.findById(card.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + card.getAccountId()));

        PaymentEvent event = new PaymentEvent(
                userId,
                card.getAccountId(),
                amount,
                account.getCurrency(),
                description,
                date
        );
        eventProducer.sendPaymentEvent(event);
    }

    private CardResponse mapToResponse(Card card) {
        Bank bank = bankRepository.findById(
                accountRepository.findById(card.getAccountId())
                        .map(Account::getBankId)
                        .orElse(0L)
        ).orElse(null);
        String bankName = bank != null ? bank.getName() : "Unknown";
        return cardMapper.toResponse(card, bankName);
    }
}
