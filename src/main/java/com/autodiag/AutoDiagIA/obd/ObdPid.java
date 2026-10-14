package com.autodiag.AutoDiagIA.obd;



public enum ObdPid {

    MONITOR_STATUS("01", "Monitor Status", 4, "bitmask"),
    FUEL_SYSTEM_STATUS("03", "Fuel System Status", 2, "status"),
    CALCULATED_LOAD("04", "Calculated Engine Load", 1, "%"),
    ENGINE_COOLANT_TEMPERATURE("05", "Engine Coolant Temperature", 1, "°C"),
    SHORT_FUEL_TRIM_1("06", "Short Term Fuel Trim Bank 1", 1, "%"),
    LONG_FUEL_TRIM_1("07", "Long Term Fuel Trim Bank 1", 1, "%"),
    SHORT_FUEL_TRIM_2("08", "Short Term Fuel Trim Bank 2", 1, "%"),
    LONG_FUEL_TRIM_2("09", "Long Term Fuel Trim Bank 2", 1, "%"),
    INTAKE_MANIFOLD_PRESSURE("0B", "Intake Manifold Pressure", 1, "kPa"),
    ENGINE_RPM("0C", "Engine RPM", 2, "rpm"),
    VEHICLE_SPEED("0D", "Vehicle Speed", 1, "km/h"),
    TIMING_ADVANCE("0E", "Timing Advance", 1, "°"),
    INTAKE_AIR_TEMPERATURE("0F", "Intake Air Temperature", 1, "°C"),
    MAF_AIR_FLOW("10", "MAF Air Flow", 2, "g/s"),
    THROTTLE_POSITION("11", "Throttle Position", 1, "%"),
    O2_SENSORS_PRESENT("13", "O2 Sensors Present", 1, "bitmask"),
    O2_SENSOR_1_B1("14", "O2 Sensor 1 Bank 1", 2, "mixed"),
    O2_SENSOR_2_B1("15", "O2 Sensor 2 Bank 1", 2, "mixed"),
    O2_SENSOR_2_B2("19", "O2 Sensor 2 Bank 2", 2, "mixed"),
    OBD_STANDARD("1C", "OBD Standard", 1, "code"),
    ENGINE_RUN_TIME("1F", "Engine Run Time", 2, "s"),
    DISTANCE_WITH_MIL("21", "Distance With MIL On", 2, "km"),
    FUEL_RAIL_PRESSURE("23", "Fuel Rail Pressure", 2, "kPa"),
    EVAP_PURGE("2E", "Commanded EVAP Purge", 1, "%"),
    FUEL_LEVEL("2F", "Fuel Level", 1, "%"),
    WARM_UPS("30", "Warm-ups Since Clear", 1, "count"),
    DISTANCE_SINCE_CLEAR("31", "Distance Since Clear", 2, "km"),
    EVAP_VAPOR_PRESSURE("32", "EVAP Vapor Pressure", 2, "Pa"),
    BAROMETRIC_PRESSURE("33", "Barometric Pressure", 1, "kPa"),
    O2_SENSOR_1_WIDE_RANGE("34", "O2 Sensor 1 Wide Range", 4, "mixed"),
    O2_SENSOR_5_WIDE_RANGE("38", "O2 Sensor 5 Wide Range", 4, "mixed"),
    CATALYST_TEMP_B1S1("3C", "Catalyst Temperature B1S1", 2, "°C"),
    CATALYST_TEMP_B2S1("3D", "Catalyst Temperature B2S1", 2, "°C"),
    MONITOR_STATUS_CYCLE("41", "Monitor Status This Drive Cycle", 4, "bitmask"),
    CONTROL_MODULE_VOLTAGE("42", "Control Module Voltage", 2, "V"),
    ABSOLUTE_LOAD("43", "Absolute Load", 2, "%"),
    COMMANDED_EQUIVALENCE_RATIO("44", "Commanded Equivalence Ratio", 2, "ratio"),
    RELATIVE_THROTTLE_POSITION("45", "Relative Throttle Position", 1, "%"),
    AMBIENT_AIR_TEMP("46", "Ambient Air Temperature", 1, "°C"),
    THROTTLE_POSITION_B("47", "Throttle Position B", 1, "%"),
    ACCELERATOR_PEDAL_POSITION_D("49", "Accelerator Pedal Position D", 1, "%"),
    ACCELERATOR_PEDAL_POSITION_E("4A", "Accelerator Pedal Position E", 1, "%"),
    COMMANDED_THROTTLE("4C", "Commanded Throttle Actuator", 1, "%"),
    FUEL_TYPE("51", "Fuel Type", 1, "code"),
    SHORT_O2_TRIM_B1("56", "Short Term Secondary O2 Trim B1", 1, "%"),
    SHORT_O2_TRIM_B2("58", "Short Term Secondary O2 Trim B2", 1, "%");

    private final String code;
    private final String description;
    private final int dataBytes;
    private final String unit;

    ObdPid(String code, String description, int dataBytes, String unit) {
        this.code = code;
        this.description = description;
        this.dataBytes = dataBytes;
        this.unit = unit;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getDataBytes() {
        return dataBytes;
    }

    public String getUnit() {
        return unit;
    }

    public static ObdPid fromString(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("PID não informado.");
        }

        String normalized = name.trim().toUpperCase();

        for (ObdPid pid : values()) {
            if (pid.name().equals(normalized)|| pid.code.equals(normalized)) {
                return pid;
            }
        }

        return switch (normalized.toLowerCase()) {
            case "rpm", "giro" -> ENGINE_RPM;
            case "speed", "velocidade" -> VEHICLE_SPEED;
            case "coolant_temperature", "temperatura",
                 "temperatura_motor" -> ENGINE_COOLANT_TEMPERATURE;
            case "load", "engine_load" -> CALCULATED_LOAD;
            case "map", "intake_pressure" -> INTAKE_MANIFOLD_PRESSURE;
            case "maf", "air_flow" -> MAF_AIR_FLOW;
            case "throttle", "acelerador" -> THROTTLE_POSITION;
            case "fuel_level", "nivel_combustivel" -> FUEL_LEVEL;
            case "battery_voltage", "voltage" -> CONTROL_MODULE_VOLTAGE;
            case "ambient_temperature" -> AMBIENT_AIR_TEMP;
            default -> throw new IllegalArgumentException(
                    "PID desconhecido: " + name);
        };
    }
}
