package com.financialapp.banks.web.dto.response;

import java.util.List;

public record BatchImportResponse(int imported, int skipped, List<String> errors) {}
