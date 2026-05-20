package com.financialapp.banks.domain.model.bank;

public enum BankName {
    GALICIA("Galicia"),
    SANTANDER("Santander"),
    BBVA("BBVA"),
    HIPOTECARIO("Hipotecario"),
    MACRO("Macro"),
    PATAGONIA("Patagonia"),
    NACION("Nación"),
    ICBC("ICBC"),
    CITIBANK("Citibank"),
    HSBC("HSBC"),
    SUPERVIELLE("Supervielle"),
    BANCO_COMAFI("Banco Comafi"),
    BANCO_DEL_CHUBUT("Banco del Chubut");

    private final String displayName;

    BankName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
