package com.autodiag.AutoDiagIA.obd;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import com.autodiag.AutoDiagIA.dto.ObdResponse;
import com.autodiag.AutoDiagIA.exception.InvalidObdRequestException;
import com.autodiag.AutoDiagIA.exception.ObdResponseException;

import java.util.Locale;


import org.springframework.stereotype.Component;


@Component 
public class ObdClient {

    private final Elm327Client elm327Client;

    public ObdClient(Elm327Client elm327Client) {
        this.elm327Client = elm327Client;

    }

    public ObdResponse readPid(ObdPid pid) {

        String command = ObdCommand.commandToMode01(pid);

        String response = elm327Client.sendCommand(command);
        System.out.println("Comando enviado: " + command);
        System.out.println("Resposta recebida: " + response + "");

        return ObdResponseParser.parse(response, pid);

    }

    public Void clearDtc(){
        String response = elm327Client.sendCommand(ObdCommand.commandToMode04());
        System.out.println("Comando enviado: " + ObdCommand.commandToMode04());
        System.out.println("Resposta recebida: " + response);

        if (response != null && response.trim().toUpperCase().startsWith("44")) {
            return null;
        }
        throw new ObdResponseException("A ECU não confirmou a limpeza dos DTCs.");
    }


    public DTCReading readDTC(String mode) {

        String command = switch (mode.toLowerCase(Locale.ROOT)) {
            case "confirmado" -> DTCMode.CONFIRMED_DTC.getCode();
            case "pendente" -> DTCMode.PENDENT_DTC.getCode();
            case "permanente" -> DTCMode.PERMANENT_DTC.getCode();

            default -> throw new InvalidObdRequestException(
                    "Tipo de DTC inválido: " + mode
            );
        };

        String response = elm327Client.sendCommand(command);

        System.out.println("Comando enviado: " + command);
        System.out.println("Resposta recebida: " + response );

        return DTCResponseParser.parse(response, command);
        }

}





