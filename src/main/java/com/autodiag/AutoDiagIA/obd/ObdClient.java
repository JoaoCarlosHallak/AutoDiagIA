package com.autodiag.AutoDiagIA.obd;

import com.autodiag.AutoDiagIA.communication.Elm327Client;

import java.io.IOException;
import java.time.Instant;

import org.springframework.stereotype.Component;


@Component 
public class ObdClient {

    private final Elm327Client elm327Client;

    public ObdClient(Elm327Client elm327Client) {
        this.elm327Client = elm327Client;

    }

    public ObdReading read(ObdPid pid) throws IOException {

        ObdCommand command =
                new ObdCommand(pid);

        String response =
                elm327Client.sendCommand(command.toCommand());
        System.out.println("Comando enviado: " + command.toCommand());
        System.out.println("Resposta recebida: [" + response + "]");

        ObdReading reading =
                ObdResponseParser.parse(response, pid);

        return reading;
    }


}

