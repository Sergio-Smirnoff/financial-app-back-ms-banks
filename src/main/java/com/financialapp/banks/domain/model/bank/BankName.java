package com.financialapp.banks.domain.model.bank;

import com.financialapp.banks.domain.exception.bank.UnsupportedBankException;

public enum BankName {
    GALICIA("Galicia", null),
    SANTANDER("Santander", null),
    BBVA("BBVA", null),
    HIPOTECARIO("Hipotecario", null),
    MACRO("Macro", null),
    PATAGONIA("Patagonia", null),
    NACION("Nación", null),
    ICBC("ICBC", null),
    CITIBANK("Citibank", null),
    HSBC("HSBC", null),
    SUPERVIELLE("Supervielle", null),
    BANCO_COMAFI("Banco Comafi", null),
    BANCO_DEL_CHUBUT("Banco del Chubut", null);

    private final String displayName;
    private final String logoUrl;

    BankName(String displayName, String logoUrl) {
        this.displayName = displayName;
        this.logoUrl = logoUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public static BankName fromString(String value) {
        try {
            return BankName.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedBankException(value);
        }
    }
}
