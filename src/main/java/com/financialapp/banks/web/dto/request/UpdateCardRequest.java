package com.financialapp.banks.web.dto.request;

import java.time.LocalDate;

public record UpdateCardRequest(
        LocalDate expiringDate,
        Integer closingDay,
        Integer dueDay
) {}
