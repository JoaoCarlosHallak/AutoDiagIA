package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import com.autodiag.AutoDiagIA.obd.ObdClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObdClientClearDtcTest {

    @Test
    void acceptsEcuConfirmationForMode04() {
        Elm327Client elm327Client = mock(Elm327Client.class);
        when(elm327Client.sendCommand("04")).thenReturn("44");

        ObdClient obdClient = new ObdClient(elm327Client);

        assertDoesNotThrow(obdClient::clearDtc);
        verify(elm327Client).sendCommand("04");
    }

    @Test
    void rejectsMode04ResponseNotConfirmedByEcu() {
        Elm327Client elm327Client = mock(Elm327Client.class);
        when(elm327Client.sendCommand("04")).thenReturn("NO DATA");

        ObdClient obdClient = new ObdClient(elm327Client);

        assertThrows(ObdResponseException.class, obdClient::clearDtc);
        verify(elm327Client).sendCommand("04");
    }
}
