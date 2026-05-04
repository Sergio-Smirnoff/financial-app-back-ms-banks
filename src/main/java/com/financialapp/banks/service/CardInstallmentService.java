package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.kafka.event.PaymentEvent;
import com.financialapp.banks.kafka.producer.BanksEventProducer;
import com.financialapp.banks.mapper.CardInstallmentMapper;
import com.financialapp.banks.model.dto.request.CardExpenseCreateRequest;
import com.financialapp.banks.model.dto.request.CardExpenseImportRequest;
import com.financialapp.banks.model.dto.response.BatchImportResponse;
import com.financialapp.banks.model.dto.response.CardInstallmentResponse;
import com.financialapp.banks.model.entity.Card;
import com.financialapp.banks.model.entity.CardInstallment;
import com.financialapp.banks.model.enums.CardBehavior;
import com.financialapp.banks.repository.CardInstallmentRepository;
import com.financialapp.banks.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CardInstallmentService {

    private final CardInstallmentRepository installmentRepository;
    private final CardRepository cardRepository;
    private final AccountService accountService;
    private final CardInstallmentMapper installmentMapper;
    private final BanksEventProducer eventProducer;

    @Transactional(readOnly = true)
    public List<CardInstallmentResponse> listByCard(Long cardId, Long userId) {
        cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
        return installmentRepository.findByCardIdOrderByDueDateAsc(cardId).stream()
                .map(installmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<CardInstallmentResponse> createExpense(Long cardId, Long userId, CardExpenseCreateRequest request) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));

        if (card.getBehavior() == CardBehavior.INSTANT_PAYMENT) {
            throw new BusinessException("Instant payment cards do not support installment-based expenses. Use a regular Transaction.");
        }

        List<CardInstallment> installments = new ArrayList<>();
        BigDecimal installmentAmount = request.totalAmount()
                .divide(BigDecimal.valueOf(request.totalInstallments()), 2, RoundingMode.HALF_UP);
        BigDecimal lastInstallmentAmount = request.totalAmount()
                .subtract(installmentAmount.multiply(BigDecimal.valueOf(request.totalInstallments() - 1)));

        for (int i = 1; i <= request.totalInstallments(); i++) {
            BigDecimal currentAmount = (i == request.totalInstallments()) ? lastInstallmentAmount : installmentAmount;
            CardInstallment installment = CardInstallment.builder()
                    .card(card)
                    .description(request.description())
                    .totalAmount(request.totalAmount())
                    .currency(request.currency())
                    .installmentNumber(i)
                    .totalInstallments(request.totalInstallments())
                    .amount(currentAmount)
                    .dueDate(request.firstDueDate().plusMonths(i - 1))
                    .paid(false)
                    .build();
            installments.add(installment);
        }

        return installmentRepository.saveAll(installments).stream()
                .map(installmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public BatchImportResponse importExpenses(Long cardId, Long userId, CardExpenseImportRequest req, boolean bypassBalance) {
        cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));

        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();

        for (CardExpenseImportRequest.ImportedExpense expense : req.expenses()) {
            Long accountId = resolveAccount(expense.currency(), req.arsAccountId(), req.usdAccountId());
            if (accountId == null) {
                skipped++;
                continue;
            }

            try {
                // Reuse existing createExpense() + payInstallment()
                List<CardInstallmentResponse> created = createExpense(cardId, userId,
                        new CardExpenseCreateRequest(
                                expense.description(), expense.amount(), expense.currency(), 1, expense.date()
                        ));
                payInstallment(cardId, created.get(0).id(), userId, accountId, expense.date(), bypassBalance);
                imported++;
            } catch (Exception e) {
                errors.add(expense.description() + ": " + e.getMessage());
            }
        }
        return new BatchImportResponse(imported, skipped, errors);
    }

    private Long resolveAccount(String currency, Long arsId, Long usdId) {
        return switch (currency.toUpperCase()) {
            case "ARS" -> arsId;
            case "USD" -> usdId;  // null if not provided -> caller skips
            default -> null;
        };
    }

    @Transactional(readOnly = true)
    public List<Integer> checkDuplicates(Long cardId, List<CardExpenseCreateRequest> expenses) {
        return IntStream.range(0, expenses.size())
                .filter(i -> {
                    CardExpenseCreateRequest req = expenses.get(i);
                    return installmentRepository.existsByCardIdAndDescriptionAndAmountAndDueDate(
                            cardId, req.description(), req.totalAmount(), req.firstDueDate());
                })
                .boxed()
                .toList();
    }

    @Transactional
    public CardInstallmentResponse payInstallment(Long cardId, Long installmentId, Long userId, Long accountId, LocalDate paidDate, boolean bypassBalance) {
        cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));

        CardInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found: " + installmentId));

        if (!installment.getCard().getId().equals(cardId)) {
            throw new BusinessException("Installment does not belong to the specified card");
        }

        if (installment.isPaid()) {
            throw new BusinessException("Installment is already paid");
        }

        // 1. Deduct funds from account (fail-fast)
        if (!bypassBalance) {
            accountService.adjustBalance(accountId, installment.getAmount().negate(), installment.getCurrency());
        }

        // 2. Mark as paid
        installment.setPaid(true);
        installment.setPaidDate(paidDate != null ? paidDate : LocalDate.now());
        CardInstallment saved = installmentRepository.save(installment);

        // 3. Emit event (only for recording transaction in finances)
        eventProducer.sendPaymentEvent(new PaymentEvent(
                userId,
                accountId,
                saved.getAmount().negate(),
                saved.getCurrency(),
                "Card Installment: " + saved.getDescription() + " (" + saved.getInstallmentNumber() + "/" + saved.getTotalInstallments() + ")",
                saved.getPaidDate()
        ));

        return installmentMapper.toResponse(saved);
    }
}
