package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.domain.usecase.card.command.RegisterCardExpenseCommand;
import com.financialapp.banks.domain.usecase.card.command.ImportCardExpensesCommand;
import com.financialapp.banks.domain.usecase.card.command.PayCardInstallmentCommand;
import com.financialapp.banks.domain.usecase.card.BatchImportResult;
import com.financialapp.banks.domain.usecase.card.ImportCardExpensesUseCase;
import com.financialapp.banks.domain.usecase.card.RegisterCardExpenseUseCase;
import com.financialapp.banks.domain.usecase.card.PayCardInstallmentUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportCardExpensesUseCaseImpl implements ImportCardExpensesUseCase {

    private final CardRepository cardRepository;
    private final RegisterCardExpenseUseCase createExpense;
    private final PayCardInstallmentUseCase payInstallment;

    @Override
    @Transactional
    public BatchImportResult execute(ImportCardExpensesCommand cmd) {
        cardRepository.findByCardNumberAndUserId(cmd.cardNumber(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", cmd.cardNumber()));

        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();

        for (ImportCardExpensesCommand.ImportedExpense expense : cmd.expenses()) {
            String accountCbu = resolveAccountCbu(expense.amount().currency(), cmd.arsAccountCbu(), cmd.usdAccountCbu());
            if (accountCbu == null) {
                skipped++;
                continue;
            }
            try {
                List<CardInstallment> created = createExpense.execute(new RegisterCardExpenseCommand(
                        cmd.cardNumber(), cmd.userId(), expense.description(), expense.amount(), 1, expense.date()));
                payInstallment.execute(new PayCardInstallmentCommand(
                        cmd.cardNumber(), created.get(0).id(), cmd.userId(), accountCbu, expense.date()));
                imported++;
            } catch (Exception e) {
                errors.add(expense.description() + ": " + e.getMessage());
            }
        }

        return new BatchImportResult(imported, skipped, errors);
    }

    private String resolveAccountCbu(Currency currency, String arsCbu, String usdCbu) {
        return switch (currency.toString().toUpperCase()) {
            case "ARS" -> arsCbu;
            case "USD" -> usdCbu;
            default -> null;
        };
    }
}
