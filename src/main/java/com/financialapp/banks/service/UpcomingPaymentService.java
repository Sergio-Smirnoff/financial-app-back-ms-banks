package com.financialapp.banks.service;

import com.financialapp.banks.model.dto.response.UpcomingPaymentResponse;
import com.financialapp.banks.model.entity.CardInstallment;
import com.financialapp.banks.model.entity.LoanInstallment;
import com.financialapp.banks.repository.CardInstallmentRepository;
import com.financialapp.banks.repository.LoanInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpcomingPaymentService {

    private final LoanInstallmentRepository loanInstallmentRepository;
    private final CardInstallmentRepository cardInstallmentRepository;

    @Transactional(readOnly = true)
    public List<UpcomingPaymentResponse> getUpcomingPayments(Long userId, LocalDate from, LocalDate to) {
        // This is simplified, ideally we filter by userId at DB level
        // But for now we fetch all and filter here or rely on current repo methods if they exist
        
        List<UpcomingPaymentResponse> results = new ArrayList<>();

        // 1. Fetch Loan Installments
        List<LoanInstallment> loanInsts = loanInstallmentRepository.findUpcomingUnpaid(from, to);
        for (LoanInstallment li : loanInsts) {
            if (li.getLoan().getUserId().equals(userId)) {
                results.add(UpcomingPaymentResponse.builder()
                        .id(li.getId())
                        .type("LOAN")
                        .description(li.getLoan().getName())
                        .amount(li.getAmount())
                        .currency(li.getLoan().getCurrency())
                        .dueDate(li.getDueDate())
                        .installmentNumber(li.getInstallmentNumber())
                        .totalInstallments(li.getLoan().getTotalInstallments())
                        .paid(li.isPaid())
                        .build());
            }
        }

        // 2. Fetch Card Installments
        // We need a method in CardInstallmentRepository for this
        List<CardInstallment> cardInsts = cardInstallmentRepository.findAll(); // TEMPORARY, fetch all and filter
        for (CardInstallment ci : cardInsts) {
            if (ci.getCard().getUserId().equals(userId) && !ci.isPaid() && 
                !ci.getDueDate().isBefore(from) && !ci.getDueDate().isAfter(to)) {
                results.add(UpcomingPaymentResponse.builder()
                        .id(ci.getId())
                        .type("CARD")
                        .description(ci.getDescription())
                        .amount(ci.getAmount())
                        .currency(ci.getCurrency())
                        .dueDate(ci.getDueDate())
                        .installmentNumber(ci.getInstallmentNumber())
                        .totalInstallments(ci.getTotalInstallments())
                        .paid(ci.isPaid())
                        .build());
            }
        }

        results.sort(Comparator.comparing(UpcomingPaymentResponse::dueDate));
        return results;
    }
}
