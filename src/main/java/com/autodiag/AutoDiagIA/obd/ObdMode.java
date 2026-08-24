package com.autodiag.AutoDiagIA.obd;


public enum ObdMode {

    CURRENT_DATA("01"),
    FREEZE_FRAME("02"),
    STORED_DTCS("03"),
    CLEAR_DTCS("04"),
    OXYGEN_SENSOR_TEST("05"),
    ON_BOARD_MONITORING("06"),
    CONTINUOUS_MONITORING("07"),
    CONTROL_OPERATION("08"),
    VEHICLE_INFORMATION("09"),
    PERMANENT_DTCS("0A");

    private final String code;

    ObdMode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}