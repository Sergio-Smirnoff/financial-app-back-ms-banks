package com.financialapp.banks.application.card.usecase;

import java.util.List;

public record BatchImportResult(int imported, int skipped, List<String> errors) {}
