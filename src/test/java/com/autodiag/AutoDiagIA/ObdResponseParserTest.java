
package com.autodiag.AutoDiagIA;

import org.junit.jupiter.api.Test;

import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.obd.ObdResponseParser;
import com.autodiag.AutoDiagIA.obd.ObdPid;

import static org.junit.jupiter.api.Assertions.*;

class ObdResponseParserTest {

    private final ObdResponseParser parser = new ObdResponseParser();

    @Test
    void deveInterpretarTemperaturaDoLiquidoDeArrefecimento() {
        ObdReading reading = parser.parse(
                "41 05 87", ObdPid.fromString("05"));

        assertEquals(95.0, reading.values().get(0).value(), 0.001);
    }

    @Test
    void deveInterpretarRotacaoDoMotor() {
        ObdReading reading = parser.parse(
                "41 0C 17 DC", ObdPid.fromString("0C"));

        assertEquals(1527.0, reading.values().get(0).value(), 0.001);
    }

    @Test
    void deveInterpretarVelocidadeSimulada() {
        ObdReading reading = parser.parse(
                "41 0D F5", ObdPid.fromString("0D"));

        assertEquals(245.0, reading.values().get(0).value(), 0.001);
    }

    @Test
    void deveInterpretarTensaoDoSensorDeOxigenio() {
        ObdReading reading = parser.parse(
                "41 14 53 FF", ObdPid.fromString("14"));

        assertEquals(0.415, reading.values().get(0).value(), 0.001);
        assertNull(reading.values().get(1).value());
    }

    @Test
    void deveInterpretarSensorDeOxigenioDeAmplaFaixa() {
        ObdReading reading = parser.parse(
                "41 34 80 A7 80 00", ObdPid.fromString("34"));

        assertEquals(1.0051, reading.values().get(0).value(), 0.001);
        assertEquals(4.0, reading.values().get(1).value(), 0.001);
    }

    @Test
    void deveInterpretarTensaoDoModulo() {
        ObdReading reading = parser.parse(
                "41 42 33 FF", ObdPid.fromString("42"));

        assertEquals(13.311, reading.values().get(0).value(), 0.001);
    }
}
