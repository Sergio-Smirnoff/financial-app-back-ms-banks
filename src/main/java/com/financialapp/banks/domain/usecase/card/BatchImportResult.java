package com.financialapp.banks.domain.usecase.card;

import java.util.List;

public record BatchImportResult(int imported, int skipped, List<String> errors) {}
