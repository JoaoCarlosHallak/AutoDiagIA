package com.autodiag.AutoDiagIA.obd;


import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import com.autodiag.AutoDiagIA.obd.ObdPid;


public class ObdResponseParser {

    public static ObdReading parse(String response, ObdPid pid) {
        try {
            if (response == null || response.isBlank()) {
                throw new IllegalArgumentException("Resposta OBD vazia.");
            }

            String[] bytes = response.trim().split("\\\s+");

            if (bytes.length >= 4
                        && bytes[0].equalsIgnoreCase("7F")
                        && bytes[1].equalsIgnoreCase("01")) {

                String rejectedPid = bytes[2];
                String errorCode = bytes[3];

                throw new ObdResponseException(
                        "ECU rejeitou o PID " + rejectedPid
                        + ". Código de resposta: " + errorCode
                );
                }            

            if (bytes.length < 2
                    || !bytes[0].equalsIgnoreCase("41")
                    || !bytes[1].equalsIgnoreCase(pid.getCode())) {
                throw new IllegalArgumentException(
                        "Resposta incompatível com o PID "
                                + pid.getCode() + ": " + response);
            }

            int expectedLength = 2 + pid.getDataBytes();

            if (bytes.length < expectedLength) {
                throw new IllegalArgumentException(
                        "Resposta incompleta para o PID " + pid.getCode());
            }






            int[] data = new int[pid.getDataBytes()];

            for (int i = 0; i < data.length; i++) {
                data[i] = hex(bytes, i + 2);
            }

            List<ObdValue> values = new ArrayList<>();

            switch (pid) {
                case FUEL_SYSTEM_STATUS -> {
                    values.add(new ObdValue(
                            "fuelSystemBank1Status", (double) data[0], "code"));
                    values.add(new ObdValue(
                            "fuelSystemBank2Status", (double) data[1], "code"));
                }

                case O2_SENSOR_1_B1,
                     O2_SENSOR_2_B1,
                     O2_SENSOR_2_B2 -> {
                    values.add(new ObdValue(
                            "voltage", data[0] * 0.005, "V"));

                    Double trim = data[1] == 0xFF
                            ? null
                            : (data[1] - 128.0) * 100.0 / 128.0;

                    values.add(new ObdValue(
                            "fuelTrim", trim, "%"));
                }

                case O2_SENSOR_1_WIDE_RANGE,
                     O2_SENSOR_5_WIDE_RANGE -> {
                    int equivalenceWord = word(data[0], data[1]);
                    int voltageWord = word(data[2], data[3]);

                    values.add(new ObdValue(
                            "equivalenceRatio",
                            equivalenceWord / 32768.0,
                            "ratio"));

                    values.add(new ObdValue(
                            "voltage",
                            voltageWord / 8192.0,
                            "V"));
                }

                case MONITOR_STATUS,
                     MONITOR_STATUS_CYCLE -> values.add(new ObdValue(
                        "bitmask",
                        Double.valueOf(unsignedInt(data)),
                        "bitmask"));

                default -> values.add(parseSingleValue(pid, data));
            }

            return new ObdReading(
                    pid.getCode(),
                    List.copyOf(values),
                    Instant.now());

        } catch (IllegalArgumentException e) {
            throw new ObdResponseException(
                    "Falha ao analisar a resposta OBD: " + response
                            + " | Motivo: " + e.getMessage(), e);
        }
    }

    private static ObdValue parseSingleValue(ObdPid pid, int[] d) {
        double value;
        String name = pid.getDescription();

        switch (pid) {
            case CALCULATED_LOAD,
                 THROTTLE_POSITION,
                 EVAP_PURGE,
                 FUEL_LEVEL,
                 RELATIVE_THROTTLE_POSITION,
                 THROTTLE_POSITION_B,
                 ACCELERATOR_PEDAL_POSITION_D,
                 ACCELERATOR_PEDAL_POSITION_E,
                 COMMANDED_THROTTLE ->
                    value = d[0] * 100.0 / 255.0;

            case ENGINE_COOLANT_TEMPERATURE,
                 INTAKE_AIR_TEMPERATURE,
                 AMBIENT_AIR_TEMP ->
                    value = d[0] - 40.0;

            case SHORT_FUEL_TRIM_1,
                 LONG_FUEL_TRIM_1,
                 SHORT_FUEL_TRIM_2,
                 LONG_FUEL_TRIM_2,
                 SHORT_O2_TRIM_B1,
                 SHORT_O2_TRIM_B2 ->
                    value = (d[0] - 128.0) * 100.0 / 128.0;

            case INTAKE_MANIFOLD_PRESSURE,
                 BAROMETRIC_PRESSURE,
                 VEHICLE_SPEED,
                 O2_SENSORS_PRESENT,
                 OBD_STANDARD,
                 FUEL_TYPE,
                 WARM_UPS ->
                    value = d[0];

            case ENGINE_RPM ->
                    value = word(d[0], d[1]) / 4.0;

            case TIMING_ADVANCE ->
                    value = d[0] / 2.0 - 64.0;

            case MAF_AIR_FLOW ->
                    value = word(d[0], d[1]) / 100.0;

            case ENGINE_RUN_TIME,
                 DISTANCE_WITH_MIL,
                 DISTANCE_SINCE_CLEAR ->
                    value = word(d[0], d[1]);

            case FUEL_RAIL_PRESSURE ->
                    value = word(d[0], d[1]) * 10.0;

            case EVAP_VAPOR_PRESSURE ->
                    value = signedWord(d[0], d[1]) / 4.0;

            case CATALYST_TEMP_B1S1,
                 CATALYST_TEMP_B2S1 ->
                    value = word(d[0], d[1]) / 10.0 - 40.0;

            case CONTROL_MODULE_VOLTAGE ->
                    value = word(d[0], d[1]) / 1000.0;

            case ABSOLUTE_LOAD ->
                    value = word(d[0], d[1]) * 100.0 / 255.0;

            case COMMANDED_EQUIVALENCE_RATIO ->
                    value = word(d[0], d[1]) / 32768.0;

            default -> throw new IllegalArgumentException(
                    "PID sem interpretação implementada: " + pid);
        }

        return new ObdValue(name, value, pid.getUnit());
    }

    private static int hex(String[] bytes, int index) {
        if (index >= bytes.length) {
            throw new IllegalArgumentException("Byte ausente.");
        }

        int value = Integer.parseInt(bytes[index], 16);

        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Byte hexadecimal inválido.");
        }

        return value;
    }

    private static int word(int a, int b) {
        return a * 256 + b;
    }

    private static int signedWord(int a, int b) {
        int value = word(a, b);
        return value >= 32768 ? value - 65536 : value;
    }

    private static long unsignedInt(int[] d) {
        return ((long) d[0] << 24)
                | ((long) d[1] << 16)
                | ((long) d[2] << 8)
                | d[3];
    }
}