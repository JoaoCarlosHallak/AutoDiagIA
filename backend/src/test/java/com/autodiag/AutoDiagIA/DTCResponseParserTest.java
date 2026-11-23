package com.autodiag.AutoDiagIA;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import com.autodiag.AutoDiagIA.obd.DTCReading;
import com.autodiag.AutoDiagIA.obd.DTCResponseParser;

class DTCResponseParserTest {

    @Test
    void deveDecodificarDtcsModo03() {

        String response = "43 02 03 00 01 15";

        DTCReading reading = DTCResponseParser.parse(response, "03");

        assertEquals("03", reading.mode());
        assertNotNull(reading.instant());

        assertEquals(2, reading.codes().size());
        assertEquals("P0300", reading.codes().get(0));
        assertEquals("P0115", reading.codes().get(1));
    }

    @Test
    void deveDecodificarOutrasCategorias() {

        String response = "43 03 52 00 95 00 C1 00";

        DTCReading reading = DTCResponseParser.parse(response, "03");

        assertEquals(3, reading.codes().size());
        assertEquals("C1200", reading.codes().get(0));
        assertEquals("B1500", reading.codes().get(1));
        assertEquals("U0100", reading.codes().get(2));
    }

    @Test
    void deveInterpretarRespostaSemDtcs() {

        String response = "43 00";

        DTCReading reading = DTCResponseParser.parse(response, "03");

        assertTrue(reading.codes().isEmpty());
    }

    @Test
    void deveIgnorarPaddingCan() {

        String response = "43 01 03 00 00 00 00";

        DTCReading reading = DTCResponseParser.parse(response, "03");

        assertEquals(1, reading.codes().size());
        assertEquals("P0300", reading.codes().get(0));
    }

    @Test
    void deveDecodificarDtcsPermanentesSemByteDeQuantidade() {

        String response = "4A 03 00 01 15";

        DTCReading reading = DTCResponseParser.parse(response, "0A");

        assertEquals("0A", reading.mode());
        assertEquals(2, reading.codes().size());
        assertEquals("P0300", reading.codes().get(0));
        assertEquals("P0115", reading.codes().get(1));
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
    void deveLancarExcecaoParaQuantidadeIncorreta() {

        assertThrows(
                ObdResponseException.class,
                () -> DTCResponseParser.parse("43 02 03 00", "03")
        );
    }
}
