package com.autodiag.AutoDiagIA.obd;

public enum ObdPid {

    ENGINE_COOLANT_TEMPERATURE(
            "05",
            "Engine Coolant Temperature",
            1,
            "°C"
    ),

    ENGINE_RPM(
            "0C",
            "Engine RPM",
            2,
            "rpm"
    ),

    VEHICLE_SPEED(
            "0D",
            "Vehicle Speed",
            1,
            "km/h"
    );

    private final String code;
    private final String description;
    private final int dataBytes;
    private final String unit;

    ObdPid(
            String code,
            String description,
            int dataBytes,
            String unit
    ) {
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



    public static ObdPid fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PID não informado.");
        }

        String normalized = value.trim().toLowerCase();

        return switch (normalized) {
            case "rpm", "engine_rpm", "giro" -> ENGINE_RPM;

            case "speed", "vehicle_speed", "velocidade" -> VEHICLE_SPEED;

            case "coolant_temperature", "engine_coolant_temperature",
                "temperatura", "temperatura_motor" -> ENGINE_COOLANT_TEMPERATURE;

            default -> throw new IllegalArgumentException(
                "PID desconhecido: " + value
            );
        };
    }




}