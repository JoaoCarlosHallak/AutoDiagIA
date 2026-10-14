package com.autodiag.AutoDiagIA.obd;


import java.util.HexFormat;

import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import com.autodiag.AutoDiagIA.obd.ObdPid;


public class ObdResponseParser {

    public static double parse(String response, ObdPid pid) {

        System.out.println("Parsing OBD response: " + response + " for PID: " + pid
        );
        try {
            if (response == null || response.isBlank()) {
                throw new IllegalArgumentException(
                    "Resposta OBD vazia."
                );
            }

            String[] bytes = response.trim().split("\\\s+");


            if (bytes.length < 3
                    || !bytes[0].equalsIgnoreCase("41")
                    || !bytes[1].equalsIgnoreCase(pid.getCode())) {
                throw new IllegalArgumentException(
                        "Resposta incompatível com o PID "
                                + pid.getCode() + ": " + response);}

            int a = hex(bytes, 2);
            int b = pid.getDataBytes() >= 2 ? hex(bytes, 3) : 0;
            int c = pid.getDataBytes() >= 3 ? hex(bytes, 4) : 0;
            int d = pid.getDataBytes() >= 4 ? hex(bytes, 5) : 0;                     
            

            return switch (pid) {
                case MONITOR_STATUS ->
                        unsignedInt(a, b, c, d);

                case FUEL_SYSTEM_STATUS -> a;

                case CALCULATED_LOAD,
                     THROTTLE_POSITION,
                     EVAP_PURGE,
                     FUEL_LEVEL,
                     RELATIVE_THROTTLE_POSITION,
                     THROTTLE_POSITION_B,
                     ACCELERATOR_PEDAL_POSITION_D,
                     ACCELERATOR_PEDAL_POSITION_E,
                     COMMANDED_THROTTLE ->
                        a * 100.0 / 255.0;

                case ENGINE_COOLANT_TEMPERATURE,
                     INTAKE_AIR_TEMPERATURE,
                     AMBIENT_AIR_TEMP -> a - 40.0;

                case SHORT_FUEL_TRIM_1,
                     LONG_FUEL_TRIM_1,
                     SHORT_FUEL_TRIM_2,
                     LONG_FUEL_TRIM_2,
                     SHORT_O2_TRIM_B1,
                     SHORT_O2_TRIM_B2 ->
                        (a - 128.0) * 100.0 / 128.0;

                case INTAKE_MANIFOLD_PRESSURE,
                     BAROMETRIC_PRESSURE -> a;

                case ENGINE_RPM -> word(a, b) / 4.0;

                case VEHICLE_SPEED -> a;

                case TIMING_ADVANCE -> a / 2.0 - 64.0;

                case MAF_AIR_FLOW -> word(a, b) / 100.0;

                case O2_SENSORS_PRESENT -> a;

                case O2_SENSOR_1_B1,
                     O2_SENSOR_2_B1,
                     O2_SENSOR_2_B2 -> a * 0.005;

                case OBD_STANDARD,
                     FUEL_TYPE,
                     WARM_UPS -> a;

                case ENGINE_RUN_TIME,
                     DISTANCE_WITH_MIL,
                     DISTANCE_SINCE_CLEAR -> word(a, b);

                case FUEL_RAIL_PRESSURE -> word(a, b) * 10.0;

                case EVAP_VAPOR_PRESSURE -> signedWord(a, b) / 4.0;

                case O2_SENSOR_1_WIDE_RANGE,
                     O2_SENSOR_5_WIDE_RANGE -> word(a, b) * 2.0 / 65536.0;

                case CATALYST_TEMP_B1S1,
                     CATALYST_TEMP_B2S1 -> word(a, b) / 10.0 - 40.0;

                case MONITOR_STATUS_CYCLE ->
                        unsignedInt(a, b, c, d);

                case CONTROL_MODULE_VOLTAGE -> word(a, b) / 1000.0;

                case ABSOLUTE_LOAD -> word(a, b) * 100.0 / 255.0;

                case COMMANDED_EQUIVALENCE_RATIO ->
                        word(a, b) * 2.0 / 65536.0;
            };

        } catch (IllegalArgumentException e) {
            throw new ObdResponseException(
                    "Falha ao analisar a resposta OBD: " + response
                            + " | Motivo: " + e.getMessage(),
                    e);
        }
    }


    private static int hex(String[] bytes, int index) {
        if (index >= bytes.length) {
            throw new IllegalArgumentException("Byte ausente.");
        }
        return Integer.parseInt(bytes[index], 16);
    }

    private static int word(int a, int b) {
        return a * 256 + b;
    }

    private static int signedWord(int a, int b) {
        int value = word(a, b);
        return value >= 32768 ? value - 65536 : value;
    }

    private static long unsignedInt(int a, int b, int c, int d) {
        return ((long) a << 24)
                | ((long) b << 16)
                | ((long) c << 8)
                | d;
    }
}