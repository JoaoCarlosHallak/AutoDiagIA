package com.autodiag.AutoDiagIA.obd;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class ObdResponseParserTest {

    @Test
    public void shouldParseRpm() {

        double rpm = ObdResponseParser.parse(
                "41 0C 1A F8",
                ObdPid.ENGINE_RPM
        );

        assertEquals(1726.0, rpm, 0.001);
    }

    @Test
    public void shouldParseCoolantTemperature() {

        double temperature = ObdResponseParser.parse(
                "41 05 5A",
                ObdPid.ENGINE_COOLANT_TEMPERATURE
        );

        assertEquals(50.0, temperature, 0.001);
    }

    @Test
    public void shouldParseVehicleSpeed() {

        double speed = ObdResponseParser.parse(
                "41 0D 50",
                ObdPid.VEHICLE_SPEED
        );

        assertEquals(80.0, speed, 0.001);
    }

    @Test
    public void shouldRejectEmptyResponse() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ObdResponseParser.parse(
                        "",
                        ObdPid.ENGINE_RPM
                )
        );
    }

    @Test
    public void shouldRejectNoData() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ObdResponseParser.parse(
                        "NO DATA",
                        ObdPid.ENGINE_RPM
                )
        );
    }

    @Test
    public void shouldRejectWrongPid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ObdResponseParser.parse(
                        "41 0D 50",
                        ObdPid.ENGINE_RPM
                )
        );
    }
}