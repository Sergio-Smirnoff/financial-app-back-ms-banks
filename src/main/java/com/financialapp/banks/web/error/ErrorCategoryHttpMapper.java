package com.financialapp.banks.web.error;

import com.financialapp.banks.domain.exception.ErrorCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorCategoryHttpMapper {

    public HttpStatus toHttpStatus(ErrorCategory category) {
        return switch (category) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
