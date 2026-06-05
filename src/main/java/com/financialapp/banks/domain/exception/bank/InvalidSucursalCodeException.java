package com.financialapp.banks.domain.exception.bank;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class InvalidSucursalCodeException extends DomainException {
    public InvalidSucursalCodeException(String value) {
        super(DomainError.INVALID_SUCURSAL_CODE,
              "Sucursal code must be exactly 4 digits",
              Map.of("sucursalCode", value == null ? "null" : value));
    }
}
