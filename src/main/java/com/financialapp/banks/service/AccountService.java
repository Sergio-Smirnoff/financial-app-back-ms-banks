package com.financialapp.banks.service;

import com.financialapp.banks.client.InvestmentsClient;
import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.mapper.AccountMapper;
import com.financialapp.banks.model.dto.request.AccountRequest;
import com.financialapp.banks.model.dto.response.AccountResponse;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.model.enums.AccountType;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;
    private final AccountMapper accountMapper;
    private final InvestmentsClient investmentsClient;

    @Transactional(readOnly = true)
    public List<AccountResponse> listByUser(Long userId) {
        return accountRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id, Long userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        return mapToResponse(account);
    }

    @Transactional
    public void adjustBalance(Long id, BigDecimal delta) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        account.setBalance(account.getBalance().add(delta));
        accountRepository.save(account);
    }

    @Transactional
    public AccountResponse create(Long userId, AccountRequest request) {
        Bank bank = bankRepository.findByIdAndUserId(request.bankId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + request.bankId()));
        if (accountRepository.existsByBankIdAndName(bank.getId(), request.name())) {
            throw new BusinessException(
                    "An account with name '" + request.name() + "' already exists in this bank");
        }
        Account account = Account.builder()
                .bankId(bank.getId())
                .userId(userId)
                .name(request.name())
                .type(request.type())
                .balance(request.balance())
                .currency(request.currency().toUpperCase())
                .isActive(request.isActive() == null ? Boolean.TRUE : request.isActive())
                .build();
        return mapToResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(Long id, Long userId, AccountRequest request) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        if (!account.getBankId().equals(request.bankId())) {
            throw new BusinessException("Changing an account's bank is not supported");
        }
        if (!account.getName().equals(request.name())
                && accountRepository.existsByBankIdAndName(account.getBankId(), request.name())) {
            throw new BusinessException(
                    "An account with name '" + request.name() + "' already exists in this bank");
        }
        account.setName(request.name());
        account.setType(request.type());
        account.setBalance(request.balance());
        account.setCurrency(request.currency().toUpperCase());
        if (request.isActive() != null) {
            account.setIsActive(request.isActive());
        }
        return mapToResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        accountRepository.delete(account);
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = accountMapper.toResponse(account);
        if (account.getType() == AccountType.INVESTMENT) {
            try {
                var valuation = investmentsClient.getValuation(account.getId()).getData();
                if (valuation != null && valuation.totalValuation() != null) {
                    return response.withBalance(valuation.totalValuation());
                }
            } catch (Exception e) {
                log.error("Failed to fetch valuation for investment account {}: {}", account.getId(), e.getMessage());
            }
        }
        return response;
    }
}
