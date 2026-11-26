package com.autodiag.AutoDiagIA.obd;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.autodiag.AutoDiagIA.dto.MonitorStatusCycleDTO;
import com.autodiag.AutoDiagIA.dto.MonitorStatusDTO;
import com.autodiag.AutoDiagIA.dto.ObdReading;
import com.autodiag.AutoDiagIA.dto.ObdResponse;
import com.autodiag.AutoDiagIA.dto.monitor.IgnitionType;
import com.autodiag.AutoDiagIA.dto.monitor.MonitorDTO;
import com.autodiag.AutoDiagIA.dto.monitor.MonitorType;
import com.autodiag.AutoDiagIA.exception.ObdResponseException;


public class ObdResponseParser {

    public static ObdResponse parse(String response, ObdPid pid) {
        try {
            if (response == null || response.isBlank()) {
                throw new IllegalArgumentException("Resposta OBD vazia.");
            }

            String[] bytes = splitHexBytes(response);

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
                throw new IllegalArgumentException("Resposta incompatível com o PID " + pid.getCode() + ": " + response);
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

            if (pid == ObdPid.MONITOR_STATUS) {
                return parseMonitorStatus(data);
            }

            if (pid == ObdPid.MONITOR_STATUS_CYCLE) {
                return parseMonitorStatusCycle(data);
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
                 COMMANDED_THROTTLE -> value = d[0] * 100.0 / 255.0;

            case ENGINE_COOLANT_TEMPERATURE,
                 INTAKE_AIR_TEMPERATURE,
                 AMBIENT_AIR_TEMP -> value = d[0] - 40.0;

            case SHORT_FUEL_TRIM_1,
                 LONG_FUEL_TRIM_1,
                 SHORT_FUEL_TRIM_2,
                 LONG_FUEL_TRIM_2,
                 SHORT_O2_TRIM_B1,
                 SHORT_O2_TRIM_B2 -> value = (d[0] - 128.0) * 100.0 / 128.0;

            case INTAKE_MANIFOLD_PRESSURE,
                 BAROMETRIC_PRESSURE,
                 VEHICLE_SPEED,
                 O2_SENSORS_PRESENT,
                 OBD_STANDARD,
                 FUEL_TYPE,
                 WARM_UPS -> value = d[0];

            case ENGINE_RPM -> value = word(d[0], d[1]) / 4.0;

            case TIMING_ADVANCE -> value = d[0] / 2.0 - 64.0;

            case MAF_AIR_FLOW -> value = word(d[0], d[1]) / 100.0;

            case ENGINE_RUN_TIME,
                 DISTANCE_WITH_MIL,
                 DISTANCE_SINCE_CLEAR -> value = word(d[0], d[1]);

            case FUEL_RAIL_PRESSURE -> value = word(d[0], d[1]) * 10.0;

            case EVAP_VAPOR_PRESSURE -> value = signedWord(d[0], d[1]) / 4.0;

            case CATALYST_TEMP_B1S1,
                 CATALYST_TEMP_B2S1 -> value = word(d[0], d[1]) / 10.0 - 40.0;

            case CONTROL_MODULE_VOLTAGE -> value = word(d[0], d[1]) / 1000.0;

            case ABSOLUTE_LOAD -> value = word(d[0], d[1]) * 100.0 / 255.0;

            case COMMANDED_EQUIVALENCE_RATIO -> value = word(d[0], d[1]) / 32768.0;

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

    private static MonitorStatusDTO parseMonitorStatus(int[] data) {

        int byteA = data[0];
        int byteB = data[1];

        boolean milOn = (byteA & 0x80) != 0;
        int confirmedDtcCount = byteA & 0x7F;

        IgnitionType ignitionType = parseIgnitionType(byteB);

        List<MonitorDTO> monitors = parseMonitors(data, ignitionType);

        return new MonitorStatusDTO(
                milOn,
                confirmedDtcCount,
                ignitionType,
                monitors
        );
    }

    private static MonitorStatusCycleDTO parseMonitorStatusCycle(int[] data) {

        int byteB = data[1];

        IgnitionType ignitionType = parseIgnitionType(byteB);

        List<MonitorDTO> monitors = parseMonitors(data, ignitionType);

        return new MonitorStatusCycleDTO(
                ignitionType,
                monitors
        );
    }

    private static IgnitionType parseIgnitionType(int byteB) {

        boolean compressionIgnition = (byteB & 0x08) != 0;

        return compressionIgnition
                ? IgnitionType.COMPRESSION_IGNITION
                : IgnitionType.SPARK_IGNITION;
    }

    private static String[] splitHexBytes(String response) {

        String normalized = response
                .replaceAll("\\s+", "")
                .toUpperCase();

        if (normalized.isEmpty() || normalized.length() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Resposta hexadecimal inválida: " + response
            );
        }

        String[] bytes = new String[normalized.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = normalized.substring(i * 2, i * 2 + 2);
        }

        return bytes;
    }


    private static List<MonitorDTO> parseMonitors(
            int[] data,
            IgnitionType ignitionType
    ) {

        if (ignitionType != IgnitionType.SPARK_IGNITION) {
            throw new IllegalArgumentException(
                    "Interpretação de monitores para ignição por compressão ainda não implementada."
            );
        }

        int byteB = data[1];
        int byteC = data[2];
        int byteD = data[3];

        List<MonitorDTO> monitors = new ArrayList<>();

        // Monitores contínuos
        addMonitor(monitors, MonitorType.MISFIRE,
                byteB, 0, 4);

        addMonitor(monitors, MonitorType.FUEL_SYSTEM,
                byteB, 1, 5);

        addMonitor(monitors, MonitorType.COMPREHENSIVE_COMPONENTS,
                byteB, 2, 6);

        // Monitores não contínuos
        addMonitor(monitors, MonitorType.CATALYST,
                byteC, 0, byteD, 0);

        addMonitor(monitors, MonitorType.HEATED_CATALYST,
                byteC, 1, byteD, 1);

        addMonitor(monitors, MonitorType.EVAPORATIVE_SYSTEM,
                byteC, 2, byteD, 2);

        addMonitor(monitors, MonitorType.SECONDARY_AIR_SYSTEM,
                byteC, 3, byteD, 3);

        addMonitor(monitors, MonitorType.OXYGEN_SENSOR,
                byteC, 5, byteD, 5);

        addMonitor(monitors, MonitorType.OXYGEN_SENSOR_HEATER,
                byteC, 6, byteD, 6);

        addMonitor(monitors, MonitorType.EGR_AND_VVT,
                byteC, 7, byteD, 7);

        return List.copyOf(monitors);
    }
    private static void addMonitor(
            List<MonitorDTO> monitors,
            MonitorType type,
            int supportByte,
            int supportBit,
            int incompleteBit
    ) {

        boolean supported = (supportByte & (1 << supportBit)) != 0;

        if (!supported) {
            monitors.add(new MonitorDTO(type, false, null));
            return;
        }

        boolean complete = (supportByte & (1 << incompleteBit)) == 0;

        monitors.add(new MonitorDTO(type, true, complete));
    }

    private static void addMonitor(
            List<MonitorDTO> monitors,
            MonitorType type,
            int supportByte,
            int supportBit,
            int statusByte,
            int statusBit
    ) {

        boolean supported = (supportByte & (1 << supportBit)) != 0;

        if (!supported) {
            monitors.add(new MonitorDTO(type, false, null));
            return;
        }

        boolean complete = (statusByte & (1 << statusBit)) == 0;

        monitors.add(new MonitorDTO(type, true, complete));
    }
}