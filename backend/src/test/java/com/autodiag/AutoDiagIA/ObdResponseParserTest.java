package com.autodiag.AutoDiagIA;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import com.autodiag.AutoDiagIA.obd.DTCReading;
import com.autodiag.AutoDiagIA.obd.DTCResponseParser;
import com.autodiag.AutoDiagIA.dto.ObdReading;
import com.autodiag.AutoDiagIA.dto.ObdResponse;
import com.autodiag.AutoDiagIA.obd.ObdResponseParser;
import com.autodiag.AutoDiagIA.obd.ObdPid;

import static org.junit.jupiter.api.Assertions.*;

class ObdResponseParserTest {

    private ObdReading parseReading(String response, String pid) {
        ObdResponse parsed = ObdResponseParser.parse(response, ObdPid.fromString(pid));
        return assertInstanceOf(ObdReading.class, parsed);
    }

    @Test
    @DisplayName("Deve interpretar corretamente a Temperatura do Líquido de Arrefecimento (PID 05)")
    void deveInterpretarTemperaturaDoLiquidoDeArrefecimento() {
        // Fórmula: A - 40 -> 135 - 40 = 95°C
        ObdReading reading = parseReading("41 05 87", "05");

        assertEquals("05", reading.pid());
        assertEquals(95.0, reading.values().getFirst().value(), 0.001);
        assertEquals("°C", reading.values().getFirst().unit());
    }

    @Test
    @DisplayName("Deve interpretar corretamente a Rotação do Motor - RPM (PID 0C)")
    void deveInterpretarRotacaoDoMotor() {
        // Fórmula: ((A * 256) + B) / 4 -> ((23 * 256) + 220) / 4 = 6108 / 4 = 1527 RPM
        ObdReading reading = parseReading("41 0C 17 DC", "0C");

        assertEquals(1527.0, reading.values().getFirst().value(), 0.001);
        assertEquals("rpm", reading.values().getFirst().unit());
    }

    @Test
    @DisplayName("Deve interpretar corretamente a Velocidade do Veículo (PID 0D)")
    void deveInterpretarVelocidadeSimulada() {
        // Fórmula: A -> F5 em hex = 245 km/h
        ObdReading reading = parseReading("41 0D F5", "0D");

        assertEquals(245.0, reading.values().getFirst().value(), 0.001);
        assertEquals("km/h", reading.values().getFirst().unit());
    }

    @Test
    @DisplayName("Deve interpretar Tensão do Sensor de Oxigênio e Fuel Trim (PID 14)")
    void deveInterpretarTensaoDoSensorDeOxigenio() {
        // A = 83 (53 hex) -> 83 * 0.005 = 0.415 V
        // B = 255 (FF hex) -> 255 é 0xFF, logo o trim deve ser null
        ObdReading reading = parseReading("41 14 53 FF", "14");

        assertEquals(2, reading.values().size());
        assertEquals(0.415, reading.values().get(0).value(), 0.001);
        assertNull(reading.values().get(1).value());
    }

    @Test
    @DisplayName("Deve interpretar Sensor de Oxigênio de Ampla Faixa (PID 34)")
    void deveInterpretarSensorDeOxigenioDeAmplaFaixa() {
        ObdReading reading = parseReading("41 34 80 A7 80 00", "34");

        assertEquals(2, reading.values().size());
        assertEquals(1.0051, reading.values().get(0).value(), 0.001);
        assertEquals(4.0, reading.values().get(1).value(), 0.001);
    }

    @Test
    @DisplayName("Deve interpretar Tensão do Módulo de Comando/ECU (PID 42)")
    void deveInterpretarTensaoDoModulo() {
        // Fórmula: ((A * 256) + B) / 1000 -> ((51 * 256) + 255) / 1000 = 13311 / 1000 = 13.311 V
        ObdReading reading = parseReading("41 42 33 FF", "42");

        assertEquals(13.311, reading.values().getFirst().value(), 0.001);
        assertEquals("V", reading.values().getFirst().unit());
    }

    @Test
    @DisplayName("Deve interpretar Pressão do Coletor de Admissão - MAP (PID 0B)")
    void deveInterpretarPressaoColetor() {
        // Fórmula: A -> 45 kPa
        ObdReading reading = parseReading("41 0B 2D", "0B");

        assertEquals(45.0, reading.values().getFirst().value(), 0.001);
        assertEquals("kPa", reading.values().getFirst().unit());
    }

    @Test
    @DisplayName("Deve interpretar Avanço de Ignição (PID 0E)")
    void deveInterpretarAvancoIgnicao() {
        // Fórmula: (A / 2) - 64 -> Se A = 156 (9C hex): (156 / 2) - 64 = 78 - 64 = 14 graus
        ObdReading reading = parseReading("41 0E 9C", "0E");

        assertEquals(14.0, reading.values().getFirst().value(), 0.001);
        assertEquals("°", reading.values().getFirst().unit());
    }

    @Test
    @DisplayName("Deve disparar exceção ao receber resposta de rejeição da ECU (NRC 7F 01)")
    void deveLancarExcecaoQuandoECURejeitaComando() {
        // Simula o erro NRC 7F 01 típico em falhas de protocolo ou comandos não suportados
        ObdResponseException exception = assertThrows(
                ObdResponseException.class,
                () -> ObdResponseParser.parse("7F 01 05 12", ObdPid.fromString("05"))
        );

        assertTrue(exception.getMessage().contains("ECU rejeitou o PID"));
    }

    @Test
    @DisplayName("Deve interpretar com sucesso Códigos de Falha DTC (Modo 03)")
    void deveInterpretarCodigosDtcAtuais() {
        // Resposta Mode 03 (43) contendo P0301, P0171 e P0420.
        String rawResponse = "43 03 01 01 71 04 20";
        
        DTCReading dtcReading = DTCResponseParser.parse(rawResponse, "03");

        assertEquals("03", dtcReading.mode());
        assertEquals(3, dtcReading.codes().size());
        assertEquals("P0301", dtcReading.codes().get(0));
        assertEquals("P0171", dtcReading.codes().get(1));
        assertEquals("P0420", dtcReading.codes().get(2));
    }

    @Test
    @DisplayName("Deve rejeitar resposta DTC com formato corrompido ou bytes ímpares")
    void deveRejeitarDtcInvalido() {
        assertThrows(
                ObdResponseException.class,
                () -> DTCResponseParser.parse("43 03 03 00 01 00", "03")
        );
    }




}