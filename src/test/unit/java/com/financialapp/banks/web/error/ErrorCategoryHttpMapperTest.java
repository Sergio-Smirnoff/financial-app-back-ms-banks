package com.financialapp.banks.web.error;

import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCategoryHttpMapperTest {

    private final ErrorCategoryHttpMapper mapper = new ErrorCategoryHttpMapper();

    @Test
    void mapsEveryCategoryToAStatus() {
        assertThat(mapper.toHttpStatus(ErrorCategory.NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(mapper.toHttpStatus(ErrorCategory.CONFLICT)).isEqualTo(HttpStatus.CONFLICT);
        assertThat(mapper.toHttpStatus(ErrorCategory.UNPROCESSABLE)).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(mapper.toHttpStatus(ErrorCategory.BAD_REQUEST)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mapper.toHttpStatus(ErrorCategory.INTERNAL)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
