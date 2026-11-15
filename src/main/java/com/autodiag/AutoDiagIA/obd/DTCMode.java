package com.autodiag.AutoDiagIA.obd;

public enum DTCMode {
    CONFIRMED_DTC("03", "DTCs confirmados", "confirmado"),
    PENDENT_DTC("07", "DTCs pendentes", "pendente"),
    PERMANENT_DTC("0A", "DTCs permanentes", "permanente");

    private final String code;
    private final String description;
    private final String alias;


    DTCMode(String code, String description, String alias) {
        this.code = code;
        this.description = description;
        this.alias = alias;
    }


    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getAlias() {
        return alias;
    }
}
