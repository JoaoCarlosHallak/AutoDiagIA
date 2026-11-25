package com.autodiag.AutoDiagIA;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import com.autodiag.AutoDiagIA.obd.DTCReading;
import com.autodiag.AutoDiagIA.obd.DTCResponseParser;

class DTCResponseParserTest {

    @Test
    void deveDecodificarDtcsModo03() {

        String response = "43 03 01 01 71 04 20";

        DTCReading reading = DTCResponseParser.parse(response, "03");

        assertEquals("03", reading.mode());
        assertNotNull(reading.instant());

        assertEquals(3, reading.codes().size());
        assertEquals("P0301", reading.codes().get(0));
        assertEquals("P0171", reading.codes().get(1));
        assertEquals("P0420", reading.codes().get(2));
    }

    @Test
    void deveDecodificarDtcsModo07() {
        DTCReading reading = DTCResponseParser.parse(
                "47 01 33 04 42 01 13",
                "07"
        );

        assertEquals(3, reading.codes().size());
        assertEquals("P0133", reading.codes().get(0));
        assertEquals("P0442", reading.codes().get(1));
        assertEquals("P0113", reading.codes().get(2));
    }

    @Test
    void deveDecodificarOutrasCategorias() {

        String response = "43 52 00 95 00 C1 00";

        DTCReading reading = DTCResponseParser.parse(response, "03");

        assertEquals(3, reading.codes().size());
        assertEquals("C1200", reading.codes().get(0));
        assertEquals("B1500", reading.codes().get(1));
        assertEquals("U0100", reading.codes().get(2));
    }

    @Test
    void deveInterpretarRespostaSemDtcs() {

        DTCReading reading = DTCResponseParser.parse("43", "03");

        assertTrue(reading.codes().isEmpty());
    }

    @Test
    void deveInterpretarRespostaPendenteSemDtcs() {
        DTCReading reading = DTCResponseParser.parse("47", "07");

        assertTrue(reading.codes().isEmpty());
    }

    @Test
    void deveIgnorarPaddingCan() {

        String response = "43 03 00 00 00 00 00";

        DTCReading reading = DTCResponseParser.parse(response, "03");

        assertEquals(1, reading.codes().size());
        assertEquals("P0300", reading.codes().get(0));
    }

    @Test
    void deveDecodificarDtcsPermanentesSemByteDeQuantidade() {

        String response = "4A 04 20 03 01 01 71";

        DTCReading reading = DTCResponseParser.parse(response, "0A");

        assertEquals("0A", reading.mode());
        assertEquals(3, reading.codes().size());
        assertEquals("P0420", reading.codes().get(0));
        assertEquals("P0301", reading.codes().get(1));
        assertEquals("P0171", reading.codes().get(2));
    }

    @Test
    void deveLancarExcecaoParaRespostaVazia() {

        assertThrows(
                ObdResponseException.class,
                () -> DTCResponseParser.parse("", "03")
        );

        assertThrows(
                ObdResponseException.class,
                () -> DTCResponseParser.parse(null, "03")
        );
    }

    @Test
    void deveLancarExcecaoParaCabecalhoIncompativel() {

        assertThrows(
                ObdResponseException.class,
                () -> DTCResponseParser.parse("47 01 03 00", "03")
        );
    }

    @Test
    void deveLancarExcecaoParaHexadecimalInvalido() {

        assertThrows(
                ObdResponseException.class,
                () -> DTCResponseParser.parse("43 01 0G 00", "03")
        );
    }

    @Test
    void deveLancarExcecaoParaDtcIncompleto() {

        assertThrows(
                ObdResponseException.class,
                () -> DTCResponseParser.parse("43 03 00 01 15 04", "03")
        );
    }
}
