package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.mapper.AccountMapper;
import com.financialapp.banks.mapper.BankMapper;
import com.financialapp.banks.model.dto.request.BankRequest;
import com.financialapp.banks.model.dto.response.AccountResponse;
import com.financialapp.banks.model.dto.response.BankResponse;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;
    private final BankMapper bankMapper;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public List<BankResponse> list(Long userId) {
        return bankRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(b -> bankMapper.toResponse(b, accountsFor(b.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public BankResponse get(Long id, Long userId) {
        Bank bank = bankRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + id));
        return bankMapper.toResponse(bank, accountsFor(bank.getId()));
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
        return bankMapper.toResponse(bankRepository.save(bank), List.of());
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
        return bankMapper.toResponse(bankRepository.save(bank), accountsFor(bank.getId()));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Bank bank = bankRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + id));
        bankRepository.delete(bank);
    }

    private List<AccountResponse> accountsFor(Long bankId) {
        return accountRepository.findByBankIdOrderByNameAsc(bankId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }
}
