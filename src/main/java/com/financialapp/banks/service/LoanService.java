package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.kafka.event.PaymentEvent;
import com.financialapp.banks.kafka.producer.BanksEventProducer;
import com.financialapp.banks.mapper.LoanInstallmentMapper;
import com.financialapp.banks.mapper.LoanMapper;
import com.financialapp.banks.model.dto.request.LoanRequest;
import com.financialapp.banks.model.dto.response.LoanInstallmentResponse;
import com.financialapp.banks.model.dto.response.LoanResponse;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Loan;
import com.financialapp.banks.model.entity.LoanInstallment;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import com.financialapp.banks.repository.LoanInstallmentRepository;
import com.financialapp.banks.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final AccountService accountService;
    private final LoanMapper loanMapper;
    private final LoanInstallmentMapper installmentMapper;
    private final BanksEventProducer eventProducer;

    @Transactional(readOnly = true)
    public List<LoanResponse> list(Long userId, Long bankId) {
        List<Loan> loans;
        if (bankId != null) {
            loans = loanRepository.findByBankId(bankId);
        } else {
            loans = loanRepository.findByUserId(userId);
        }
        return loans.stream().map(loanMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanInstallmentResponse> getInstallments(Long loanId, Long userId) {
        loanRepository.findByIdAndUserId(loanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));
        return installmentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream()
                .map(installmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public LoanResponse create(Long userId, LoanRequest request) {
        bankRepository.findByIdAndUserId(request.bankId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + request.bankId()));

        Account destinationAccount = accountRepository.findByIdAndUserId(request.destinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found: " + request.destinationAccountId()));

        if (!destinationAccount.getBankId().equals(request.bankId())) {
            throw new BusinessException("Destination account does not belong to the selected bank");
        }
        
        Loan loan = Loan.builder()
                .bankId(request.bankId())
                .userId(userId)
                .name(request.name())
                .principal(request.principal())
                .currency(destinationAccount.getCurrency().toUpperCase())
                .interestRate(request.interestRate())
                .totalInstallments(request.totalInstallments())
                .remainingInstallments(request.totalInstallments())
                .startDate(request.startDate())
                .active(true)
                .build();

        loan = loanRepository.save(loan);

        // Credit principal to destination account
        accountService.adjustBalance(destinationAccount.getId(), request.principal(), loan.getCurrency());

        // Record the deposit in finances
        eventProducer.sendPaymentEvent(new PaymentEvent(
                userId,
                destinationAccount.getId(),
                request.principal(),
                loan.getCurrency(),
                "Loan Deposit: " + loan.getName(),
                LocalDate.now()
        ));

        // Simple amortization: (principal * (1 + interest/100)) / installments
        BigDecimal totalWithInterest = request.principal()
                .multiply(BigDecimal.ONE.add(request.interestRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
        
        BigDecimal installmentAmount = totalWithInterest
                .divide(BigDecimal.valueOf(request.totalInstallments()), 2, RoundingMode.DOWN);
        
        BigDecimal lastInstallmentAmount = totalWithInterest
                .subtract(installmentAmount.multiply(BigDecimal.valueOf(request.totalInstallments() - 1)));

        List<LoanInstallment> installments = new ArrayList<>();
        for (int i = 1; i <= request.totalInstallments(); i++) {
            BigDecimal currentAmount = (i == request.totalInstallments()) ? lastInstallmentAmount : installmentAmount;
            LoanInstallment inst = LoanInstallment.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .amount(currentAmount)
                    .dueDate(request.startDate().plusMonths(i - 1))
                    .paid(false)
                    .build();
            installments.add(inst);
        }
        
        installmentRepository.saveAll(installments);
        return loanMapper.toResponse(loan);
    }

    @Transactional
    public LoanInstallmentResponse payInstallment(Long loanId, Long installmentId, Long userId, Long accountId, LocalDate paidDate) {
        Loan loan = loanRepository.findByIdAndUserId(loanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        if (!loan.isActive()) {
            throw new BusinessException("Loan is already closed");
        }

        LoanInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found: " + installmentId));

        if (!installment.getLoan().getId().equals(loanId)) {
            throw new BusinessException("Installment does not belong to the specified loan");
        }

        if (installment.isPaid()) {
            throw new BusinessException("Installment is already paid");
        }

        // 1. Deduct funds from selected account (fail-fast)
        accountService.adjustBalance(accountId, installment.getAmount().negate(), loan.getCurrency());

        // 2. Mark as paid
        installment.setPaid(true);
        installment.setPaidDate(paidDate != null ? paidDate : LocalDate.now());
        installment = installmentRepository.save(installment);

        loan.setRemainingInstallments(loan.getRemainingInstallments() - 1);
        if (loan.getRemainingInstallments() == 0) {
            loan.setActive(false);
        }
        loanRepository.save(loan);

        // 3. Emit payment event to update finances
        PaymentEvent event = new PaymentEvent(
                userId,
                accountId,
                installment.getAmount(),
                loan.getCurrency(),
                "Loan Payment: " + loan.getName() + " (Installment " + installment.getInstallmentNumber() + ")",
                installment.getPaidDate()
        );
        eventProducer.sendPaymentEvent(event);

        return installmentMapper.toResponse(installment);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Loan loan = loanRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
        loanRepository.delete(loan);
    }
}
