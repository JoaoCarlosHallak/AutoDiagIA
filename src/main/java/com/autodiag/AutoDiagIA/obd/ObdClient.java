package com.autodiag.AutoDiagIA.obd;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import java.io.IOException;
import java.util.Locale;


import org.springframework.stereotype.Component;


@Component 
public class ObdClient {

    private final Elm327Client elm327Client;

    public ObdClient(Elm327Client elm327Client) {
        this.elm327Client = elm327Client;

    }

    public ObdReading readPid(ObdPid pid) throws IOException {

        String command = ObdCommand.commandToMode01(pid);

        String response = elm327Client.sendCommand(command);
        System.out.println("Comando enviado: " + command);
        System.out.println("Resposta recebida: [" + response + "]");


        try {
            ObdReading reading =
                    ObdResponseParser.parse(response, pid);
            return reading;
        } catch (IllegalArgumentException e) {
            throw new ObdResponseException("Falha ao ler o PID " + pid.getCode(), e);
        }
    }


    public DTCReading readDTC(String mode) throws IOException {

        String command = switch (mode.toLowerCase(Locale.ROOT)) {
            case "confirmado" -> DTCMode.CONFIRMED_DTC.getCode();
            case "pendente" -> DTCMode.PENDENT_DTC.getCode();
            case "permanente" -> DTCMode.PERMANENT_DTC.getCode();

            default -> throw new IllegalArgumentException(
                    "Tipo de DTC inválido: " + mode
            );
        };

        String response = elm327Client.sendCommand(command);

        System.out.println("Comando enviado: " + command);
        System.out.println("Resposta recebida: [" + response + "]");

        try {
            return DTCResponseParser.parse(response, command);
        } catch (IllegalArgumentException e) {
            throw new ObdResponseException(
                    "Falha ao ler DTC no modo " + command, e
            );
        }
    }
}





