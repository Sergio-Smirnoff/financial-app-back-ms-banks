package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.mapper.AccountMapper;
import com.financialapp.banks.mapper.BankMapper;
import com.financialapp.banks.model.dto.request.BankRequest;
import com.financialapp.banks.model.dto.response.AccountResponse;
import com.financialapp.banks.model.dto.response.BankResponse;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import com.financialapp.banks.repository.CardRepository;
import com.financialapp.banks.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final LoanRepository loanRepository;
    private final BankMapper bankMapper;
    private final AccountService accountService;

    @Transactional(readOnly = true)
    public List<BankResponse> list(Long userId) {
        return bankRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BankResponse get(Long id, Long userId) {
        Bank bank = bankRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + id));
        return mapToResponse(bank);
    }

    @Transactional
    public BankResponse create(Long userId, BankRequest request) {
        if (bankRepository.existsByUserIdAndName(userId, request.name())) {
            throw new BusinessException("A bank with name '" + request.name() + "' already exists");
        }
        Bank bank = Bank.builder()
                .userId(userId)
                .name(request.name())
                .logoUrl(request.logoUrl())
                .build();
        return mapToResponse(bankRepository.save(bank));
    }

    @Transactional
    public BankResponse update(Long id, Long userId, BankRequest request) {
        Bank bank = bankRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + id));
        if (!bank.getName().equals(request.name())
                && bankRepository.existsByUserIdAndName(userId, request.name())) {
            throw new BusinessException("A bank with name '" + request.name() + "' already exists");
        }
        bank.setName(request.name());
        bank.setLogoUrl(request.logoUrl());
        return mapToResponse(bankRepository.save(bank));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Bank bank = bankRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + id));
        bankRepository.delete(bank);
    }

    private BankResponse mapToResponse(Bank bank) {
        List<Account> accounts = accountRepository.findByBankIdOrderByNameAsc(bank.getId());
        List<AccountResponse> accountResponses = accounts.stream()
                .map(a -> accountService.get(a.getId(), bank.getUserId())) // use accountService.get to include live valuation
                .toList();

        Map<String, BigDecimal> totalBalances = accountResponses.stream()
                .collect(Collectors.groupingBy(
                        AccountResponse::currency,
                        Collectors.reducing(BigDecimal.ZERO, AccountResponse::balance, BigDecimal::add)
                ));

        int cardsCount = accounts.stream()
                .mapToInt(a -> cardRepository.countByAccountId(a.getId()))
                .sum();

        int loansCount = accounts.stream()
                .mapToInt(a -> loanRepository.countByAccountId(a.getId()))
                .sum();

        return bankMapper.toResponse(bank, accountResponses, totalBalances, cardsCount, loansCount);
    }
}
