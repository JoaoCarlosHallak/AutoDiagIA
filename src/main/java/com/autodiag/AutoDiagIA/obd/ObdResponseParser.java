package com.autodiag.AutoDiagIA.obd;

public class ObdResponseParser {

    public static double parse(String response, ObdPid pid) {

        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Resposta OBD vazia.");
        }

        String[] bytes = response.trim().split("\\s+");

        if (bytes.length < 3) {
            throw new IllegalArgumentException(
                    "Resposta OBD inválida: " + response
            );
        }

        if (!bytes[0].equalsIgnoreCase("41")) {
            throw new IllegalArgumentException(
                    "Resposta não corresponde ao Mode 01: " + response
            );
        }

        if (!bytes[1].equalsIgnoreCase(pid.getCode())) {
            throw new IllegalArgumentException(
                    "PID inesperado: " + response
            );
        }

        return switch (pid) {

            case ENGINE_RPM -> parseRpm(bytes);

            case VEHICLE_SPEED -> parseVehicleSpeed(bytes);

            case ENGINE_COOLANT_TEMPERATURE ->
                    parseCoolantTemperature(bytes);
        };
    }

    private static double parseRpm(String[] bytes) {

        int a = Integer.parseInt(bytes[2], 16);
        int b = Integer.parseInt(bytes[3], 16);

        return ((a * 256) + b) / 4.0;
    }

    private static double parseVehicleSpeed(String[] bytes) {

        int a = Integer.parseInt(bytes[2], 16);

        return a;
    }

    private static double parseCoolantTemperature(String[] bytes) {

        int a = Integer.parseInt(bytes[2], 16);

        return a - 40;
    }
}

 /*   No caso do RPM, o ELM327 responda:
41 0C 1A F8
O parser faz isso:
1. Separa os bytes
bytes[0] = "41"
bytes[1] = "0C"
bytes[2] = "1A"
bytes[3] = "F8"
interprete essa String como um número hexadecimal.
((26 × 256) + 248) / 4
= (6656 + 248) / 4
= 6904 / 4
= 1726 RPM */
