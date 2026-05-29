package com.financialapp.banks.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain service computing loan installment amounts. Stateless and framework-free.
 * The annual rate is expressed as a percentage (e.g. {@code 12} = 12% per year).
 */
public final class LoanAmortization {

    private LoanAmortization() {
    }

    /**
     * Fixed monthly installment under the French (constant-payment) system.
     * A zero annual rate divides the principal evenly. Result is scaled to 2 decimals, HALF_UP.
     */
    public static BigDecimal frenchInstallment(BigDecimal principal, BigDecimal annualRatePct, int installments) {
        if (annualRatePct.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(installments), 2, RoundingMode.HALF_UP);
        }
        BigDecimal monthlyRate = annualRatePct.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        double r = monthlyRate.doubleValue();
        double pow = Math.pow(1.0 + r, installments);
        return principal.multiply(BigDecimal.valueOf(r * pow / (pow - 1.0)))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
