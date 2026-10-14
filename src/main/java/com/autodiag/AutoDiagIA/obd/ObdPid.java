package com.autodiag.AutoDiagIA.obd;



public enum ObdPid {

    MONITOR_STATUS("01", "Status do Monitor", 4, "máscara de bits"),
    FUEL_SYSTEM_STATUS("03", "Status do Sistema de Combustível", 2, "status"),
    CALCULATED_LOAD("04", "Carga Calculada do Motor", 1, "%"),
    ENGINE_COOLANT_TEMPERATURE("05", "Temperatura do Líquido de Arrefecimento do Motor", 1, "°C"),
    SHORT_FUEL_TRIM_1("06", "Ajuste de Combustível de Curto Prazo Banco 1", 1, "%"),
    LONG_FUEL_TRIM_1("07", "Ajuste de Combustível de Longo Prazo Banco 1", 1, "%"),
    SHORT_FUEL_TRIM_2("08", "Ajuste de Combustível de Curto Prazo Banco 2", 1, "%"),
    LONG_FUEL_TRIM_2("09", "Ajuste de Combustível de Longo Prazo Banco 2", 1, "%"),
    INTAKE_MANIFOLD_PRESSURE("0B", "Pressão do Coletor de Admissão", 1, "kPa"),
    ENGINE_RPM("0C", "Rotação do Motor", 2, "rpm"),
    VEHICLE_SPEED("0D", "Velocidade do Veículo", 1, "km/h"),
    TIMING_ADVANCE("0E", "Avanço de Ignição", 1, "°"),
    INTAKE_AIR_TEMPERATURE("0F", "Temperatura do Ar de Admissão", 1, "°C"),
    MAF_AIR_FLOW("10", "Fluxo de Ar MAF", 2, "g/s"),
    THROTTLE_POSITION("11", "Posição da Borboleta", 1, "%"),
    O2_SENSORS_PRESENT("13", "Sensores de O2 Presentes", 1, "máscara de bits"),
    O2_SENSOR_1_B1("14", "Sensor de O2 1 Banco 1", 2, "misto"),
    O2_SENSOR_2_B1("15", "Sensor de O2 2 Banco 1", 2, "misto"),
    O2_SENSOR_2_B2("19", "Sensor de O2 2 Banco 2", 2, "misto"),
    OBD_STANDARD("1C", "Padrão OBD", 1, "código"),
    ENGINE_RUN_TIME("1F", "Tempo de Funcionamento do Motor", 2, "s"),
    DISTANCE_WITH_MIL("21", "Distância com a MIL Acesa", 2, "km"),
    FUEL_RAIL_PRESSURE("23", "Pressão do Rail de Combustível", 2, "kPa"),
    EVAP_PURGE("2E", "Purga EVAP Comandada", 1, "%"),
    FUEL_LEVEL("2F", "Nível de Combustível", 1, "%"),
    WARM_UPS("30", "Aquecimentos Desde a Limpeza", 1, "contagem"),
    DISTANCE_SINCE_CLEAR("31", "Distância Desde a Limpeza", 2, "km"),
    EVAP_VAPOR_PRESSURE("32", "Pressão de Vapor do EVAP", 2, "Pa"),
    BAROMETRIC_PRESSURE("33", "Pressão Barométrica", 1, "kPa"),
    O2_SENSOR_1_WIDE_RANGE("34", "Sensor de O2 1 Banda Larga", 4, "misto"),
    O2_SENSOR_5_WIDE_RANGE("38", "Sensor de O2 5 Banda Larga", 4, "misto"),
    CATALYST_TEMP_B1S1("3C", "Temperatura do Catalisador B1S1", 2, "°C"),
    CATALYST_TEMP_B2S1("3D", "Temperatura do Catalisador B2S1", 2, "°C"),
    MONITOR_STATUS_CYCLE("41", "Status do Monitor neste Ciclo de Condução", 4, "máscara de bits"),
    CONTROL_MODULE_VOLTAGE("42", "Tensão do Módulo de Controle", 2, "V"),
    ABSOLUTE_LOAD("43", "Carga Absoluta", 2, "%"),
    COMMANDED_EQUIVALENCE_RATIO("44", "Razão de Equivalência Comandada", 2, "razão"),
    RELATIVE_THROTTLE_POSITION("45", "Posição Relativa da Borboleta", 1, "%"),
    AMBIENT_AIR_TEMP("46", "Temperatura do Ar Ambiente", 1, "°C"),
    THROTTLE_POSITION_B("47", "Posição da Borboleta B", 1, "%"),
    ACCELERATOR_PEDAL_POSITION_D("49", "Posição do Pedal do Acelerador D", 1, "%"),
    ACCELERATOR_PEDAL_POSITION_E("4A", "Posição do Pedal do Acelerador E", 1, "%"),
    COMMANDED_THROTTLE("4C", "Atuador da Borboleta Comandado", 1, "%"),
    FUEL_TYPE("51", "Tipo de Combustível", 1, "código"),
    SHORT_O2_TRIM_B1("56", "Ajuste Secundário de O2 de Curto Prazo B1", 1, "%"),
    SHORT_O2_TRIM_B2("58", "Ajuste Secundário de O2 de Curto Prazo B2", 1, "%");

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