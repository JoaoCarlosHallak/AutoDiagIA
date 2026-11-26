
package com.autodiag.AutoDiagIA.obd;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.autodiag.AutoDiagIA.exception.InvalidObdRequestException;
import com.autodiag.AutoDiagIA.exception.ObdResponseException;

public final class DTCResponseParser {

    private DTCResponseParser() {
    }

    public static DTCReading parse(String response, String mode) {

        if (response == null || response.isBlank()) {
            throw new ObdResponseException("Resposta DTC vazia.");
        }

        String normalized = response
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);

        if (!normalized.matches("[0-9A-F]+")) {
            throw new ObdResponseException(
                    "Resposta contém caracteres que não são hexadecimais."
            );
        }

        if (normalized.length() % 2 != 0) {
            throw new ObdResponseException(
                    "Resposta hexadecimal com quantidade ímpar de caracteres."
            );
        }

        String normalizedMode = mode.toUpperCase(Locale.ROOT);

        String expectedHeader = switch (normalizedMode) {
            case "03" -> "43";
            case "07" -> "47";
            case "0A" -> "4A";
            default -> throw new InvalidObdRequestException(
                    "Mode DTC não suportado: " + mode
            );
        };

        if (!normalized.startsWith(expectedHeader)) {
            throw new ObdResponseException(
                    "Resposta incompatível. Esperado: " + expectedHeader
                            + " | Recebido: " + normalized
            );
        }

        // Remove o byte de resposta positiva.
        String data = normalized.substring(2);

        // nenhum DTC identificado.
        if (data.equals("00")) {
            return new DTCReading(
                    normalizedMode,
                    List.of(),
                    Instant.now()
            );
        }

        if (data.length() % 4 != 0) {
            throw new ObdResponseException(
                    "Quantidade de dados DTC inválida."
            );
        }

        List<String> dtcs = new ArrayList<>();

        for (int i = 0; i < data.length(); i += 4) {

            int a = Integer.parseInt(data.substring(i, i + 2), 16);
            int b = Integer.parseInt(data.substring(i + 2, i + 4), 16);

            // 00 00 representa ausência de código.
            if (a == 0 && b == 0) {
                continue;
            }

            dtcs.add(decodeDtc(a, b));
        }

        return new DTCReading(
                normalizedMode,
                List.copyOf(dtcs),
                Instant.now()
        );
    }

    private static String decodeDtc(int a, int b) {

        char[] prefixes = {'P', 'C', 'B', 'U'};

        int category = (a & 0xC0) >> 6;
        int firstDigit = (a & 0x30) >> 4;
        int secondDigit = a & 0x0F;
        int thirdDigit = (b & 0xF0) >> 4;
        int fourthDigit = b & 0x0F;

        return String.format(
                "%c%d%X%X%X",
                prefixes[category],
                firstDigit,
                secondDigit,
                thirdDigit,
                fourthDigit
        );
    }
}
